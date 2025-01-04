/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

'use strict';

const ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
const Opcodes = Java.type('org.objectweb.asm.Opcodes');

// we can't run ASMAPI.loadData in the global context, so we do it here
// this function is called inside of initializeCoreMod
const replacements = [];
function initReplacements() {
    ASMAPI.log('DEBUG', 'Gathering Forge method redirector replacements');
    replacements.push({
        // finalizeSpawn redirection to ForgeEventFactory.onFinalizeSpawn
        'type': ASMAPI.MethodType.VIRTUAL,
        'name': 'finalizeSpawn',
        'desc': '(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;',
        'targets': ASMAPI.loadData('coremods/finalize_spawn_targets.json'),
        'factory': function(insn) {
            return ASMAPI.buildMethodCall(
                ASMAPI.MethodType.STATIC,
                'net/minecraftforge/event/ForgeEventFactory',
                'onFinalizeSpawn',
                '(Lnet/minecraft/world/entity/Mob;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;');
        }
    });
}

function initializeCoreMod() {
    initReplacements();

    return {
        'forge_method_redirector': {
            'target': {
                'type': 'CLASS',
                'names': getTargets
            },
            'transformer': applyMethodRedirects
        }
    }
}

function getTargets(classes) {
    const mergedTargets = [];
    for (let replacement of replacements) {
        for (let target of replacement.targets) {
            mergedTargets.push(target.class);
        }
    }

    return mergedTargets;
}

function applyMethodRedirects(clazz) {
    for (let replacement of replacements) {
        for (let methodString of getClassTargetMethods(clazz, replacement)) {
            // cut up the string since we've put both the name and desc as the target
            const splitPos = methodString.indexOf('(');
            const methodName = methodString.substring(0, splitPos);
            const methodDesc = methodString.substring(splitPos);
            const method = ASMAPI.findMethodNode(clazz, methodName, methodDesc);

            // if we can't find the method, get out now!
            if (method === null) {
                ASMAPI.log('ERROR', 'Failed to redirect method call for {}! Method {} not found in class {}! This is a Forge bug, and is likely due to a Minecraft update changing something.', replacement.name, methodString, clazz.name);
                continue;
            }

            for (let insn of method.instructions) {
                if (shouldReplace(insn, replacement)) {
                    const redirection = replacement.factory(insn);
                    ASMAPI.log('DEBUG', 'Redirecting method call {}{} to {}{} inside of {}.{}', insn.name, insn.desc, redirection.name, redirection.desc, clazz.name, method.name);
                    ASMAPI.insertInsn(method, insn, redirection, ASMAPI.InsertMode.REMOVE_ORIGINAL);
                }
            }
        }
    }

    return clazz;
}


/* HELPER FUNCTIONS FOR TARGET SEARCHING */

function shouldReplace(insn, replacement) {
    return insn.getOpcode() === replacement.type.toOpcode()
        && insn.name === replacement.name
        && insn.desc === replacement.desc;
}

function getClassTargetMethods(clazz, replacement) {
    for (let t of replacement.targets) {
        if (t.class === clazz.name) {
            const targets = [];

            // declared methods
            for (let method of t.methods) {
                targets.push(method);
            }

            // synthetic methods
            for (let method of clazz.methods) {
                if ((method.access & Opcodes.ACC_SYNTHETIC) != 0) {
                    targets.push(method.name + method.desc);
                }
            }

            return targets;
        }
    }
}
