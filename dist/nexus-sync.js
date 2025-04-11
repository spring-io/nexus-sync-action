"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (k !== "default" && Object.prototype.hasOwnProperty.call(mod, k)) __createBinding(result, mod, k);
    __setModuleDefault(result, mod);
    return result;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.run = void 0;
const core = __importStar(require("@actions/core"));
const util_1 = require("util");
const handler_1 = require("./handler");
const interfaces_1 = require("./interfaces");
const logging_1 = require("./logging");
const utils_1 = require("./utils");
async function run() {
    try {
        const username = inputNotRequired('username') || '';
        const password = inputNotRequired('password') || '';
        const token = inputNotRequired('token') || '';
        if (!token) {
            if (!username && !password) {
                throw new Error('Either username and password or token needs to be set.');
            }
        }
        const create = inputNotRequired('create') === 'true' ? true : false;
        const stagingProfileName = inputRequired('staging-profile-name');
        const stagingRepoId = inputNotRequired('staging-repo-id') || undefined;
        const upload = inputNotRequired('upload') === 'true' ? true : false;
        const uploadParallel = (0, utils_1.numberValue)(inputNotRequired('upload-parallel'), 1);
        const close = inputNotRequired('close') === 'true' ? true : false;
        const dropIfFailure = inputNotRequired('drop-if-failure') === 'true' ? true : false;
        const closeTimeout = (0, utils_1.numberValue)(inputNotRequired('close-timeout'), 600);
        const release = inputNotRequired('release') === 'true' ? true : false;
        const releaseAutoDrop = inputNotRequired('release-auto-drop') === 'false' ? false : true;
        const releaseTimeout = (0, utils_1.numberValue)(inputNotRequired('release-timeout'), 600);
        const url = inputNotRequired('url') || 'https://s01.oss.sonatype.org';
        const dir = inputNotRequired('dir') || 'nexus';
        const generateChecksums = inputNotRequired('generate-checksums') === 'true' ? true : false;
        const generateChecksumsConfigData = inputNotRequired('generate-checksums-config') || undefined;
        const generateChecksumsConfig = generateChecksumsConfigData
            ? JSON.parse(generateChecksumsConfigData)
            : interfaces_1.DEFAULT_GENERATE_CHECKSUM_CONFIG;
        const nexusTimeout = (0, utils_1.numberValue)(inputNotRequired('nexus-timeout'), 0);
        if (uploadParallel < 1) {
            throw new Error(`'upload-parallel' needs to be higher than 0, was ${uploadParallel}`);
        }
        const actionOptions = {
            create,
            stagingProfileName,
            stagingRepoId,
            upload,
            uploadParallel,
            close,
            closeTimeout: closeTimeout * 1000,
            dropIfFailure,
            release,
            releaseAutoDrop,
            releaseTimeout: releaseTimeout * 1000,
            dir,
            nexusServer: {
                username,
                password,
                token,
                url,
                timeout: nexusTimeout * 1000
            },
            generateChecksums,
            generateChecksumsConfig
        };
        await (0, handler_1.handle)(actionOptions);
    }
    catch (error) {
        (0, logging_1.logDebug)((0, util_1.inspect)(error));
        if (error instanceof Error) {
            core.setFailed(error.message);
        }
    }
}
exports.run = run;
function inputRequired(id) {
    return core.getInput(id, { required: true });
}
function inputNotRequired(id) {
    return core.getInput(id, { required: false });
}
//# sourceMappingURL=nexus-sync.js.map