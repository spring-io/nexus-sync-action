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
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.waitRepoState = exports.stagingRepositoryActivity = exports.dropStagingRepo = exports.releaseStagingRepo = exports.closeStagingRepo = exports.uploadFiles = exports.createStagingRepo = void 0;
const core = __importStar(require("@actions/core"));
const utils_1 = require("./utils");
const logging_1 = require("./logging");
const axios_1 = __importDefault(require("axios"));
/**
 * Create a staging repo and return its id for furher operations.
 */
async function createStagingRepo(nexusClient, actionOptions) {
    (0, logging_1.logDebug)('Resolving staging profile id');
    const stagingProfileId = await nexusClient.getStagingProfileId(actionOptions.stagingProfileName);
    const data = {
        data: {
            description: 'Create repo'
        }
    };
    (0, logging_1.logDebug)('Creating staging repo');
    const startResponse = await nexusClient.createStagingRepo(stagingProfileId, data);
    core.setOutput('staged-repository-id', startResponse.data.stagedRepositoryId);
    return startResponse.data.stagedRepositoryId;
}
exports.createStagingRepo = createStagingRepo;
/**
 * Upload all files from a give directory into a staging repository.
 */
async function uploadFiles(nexusClient, dir, stagedRepositoryId, uploadParallel) {
    (0, logging_1.logDebug)(`Uploading files to staging repo, uploadParallel=${uploadParallel}`);
    const files = await (0, utils_1.findFiles)(dir);
    const len = files.length;
    // do uploads in defined chunks
    for (let i = 0; i < len; i += uploadParallel) {
        const promises = files
            .slice(i, i + uploadParallel)
            .map(file => nexusClient.deployByRepository(file, stagedRepositoryId));
        await Promise.all(promises);
    }
}
exports.uploadFiles = uploadFiles;
/**
 * Close staging repository.
 */
async function closeStagingRepo(nexusClient, actionOptions, stagedRepositoryId) {
    (0, logging_1.logDebug)('Closing staging repo');
    const stagingProfileId = await nexusClient.getStagingProfileId(actionOptions.stagingProfileName);
    await nexusClient.closeStagingRepo(stagingProfileId, {
        data: {
            description: 'Close repo',
            stagedRepositoryId
        }
    });
}
exports.closeStagingRepo = closeStagingRepo;
/**
 * Release staging repository.
 */
async function releaseStagingRepo(nexusClient, actionOptions, stagedRepositoryId) {
    (0, logging_1.logDebug)('Releasing staging repo');
    await nexusClient.bulkPromoteStagingRepos({
        data: {
            description: 'Release repo',
            stagedRepositoryIds: [stagedRepositoryId],
            autoDropAfterRelease: actionOptions.releaseAutoDrop
        }
    });
}
exports.releaseStagingRepo = releaseStagingRepo;
/**
 * Drop staging repository.
 */
async function dropStagingRepo(nexusClient, stagedRepositoryId) {
    (0, logging_1.logDebug)('Dropping staging repo');
    await nexusClient.dropStagingRepo(stagedRepositoryId, {
        data: {
            description: 'Release repo',
            stagedRepositoryId
        }
    });
}
exports.dropStagingRepo = dropStagingRepo;
/**
 * Get repo activity.
 */
async function stagingRepositoryActivity(nexusClient, stagedRepositoryId) {
    (0, logging_1.logDebug)('Getting staging repo activity');
    return await nexusClient.stagingRepositoryActivity(stagedRepositoryId);
}
exports.stagingRepositoryActivity = stagingRepositoryActivity;
/**
 * Wait with a given timeout for a repo state.
 */
async function waitRepoState(nexusClient, repositoryIdKey, state, timeout, sleep = 10000) {
    return new Promise(async (resolve, reject) => {
        let now = Date.now();
        const until = now + timeout;
        (0, logging_1.logDebug)(`Waiting until ${new Date(until)} from now ${new Date(now)}`);
        let seenRepo = false;
        while (until > now) {
            // when repo goes away, i.e. with auto-drop, underlying rest call
            // will return 404, so don't throw if we've seen repo being there
            // first time and then assume it's state is ok.
            let repository;
            try {
                repository = await nexusClient.stagingRepository(repositoryIdKey);
                seenRepo = true;
            }
            catch (error) {
                if (axios_1.default.isAxiosError(error)) {
                    if (error.response?.status === 404 && seenRepo) {
                        resolve();
                        return;
                    }
                }
            }
            if (repository) {
                (0, logging_1.logInfo)(`Repo state ${repository.type}`);
                if (repository && repository.type === state) {
                    resolve();
                    return;
                }
                if (repository.notifications > 0) {
                    reject(new Error(`Last operation failed with ${repository.notifications} notifications`));
                    return;
                }
                await (0, utils_1.delayPromise)(sleep);
                now = Date.now();
            }
        }
        reject(new Error('Timeout waiting state'));
    });
}
exports.waitRepoState = waitRepoState;
//# sourceMappingURL=nexus-utils.js.map