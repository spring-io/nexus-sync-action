"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.handle = void 0;
const util_1 = require("util");
const logging_1 = require("./logging");
const nexus_utils_1 = require("./nexus-utils");
const nexus2_client_1 = require("./nexus2-client");
const utils_1 = require("./utils");
async function handle(actionOptions) {
    const nexusClient = new nexus2_client_1.Nexus2Client(actionOptions.nexusServer);
    (0, logging_1.logDebug)(`Using nexus server ${actionOptions.nexusServer.url} with timeout ${actionOptions.nexusServer.timeout}`);
    // initial state calculated from a given options
    const handlerState = {
        needChecksum: actionOptions.generateChecksums,
        needCreate: actionOptions.create,
        stagingRepoId: actionOptions.stagingRepoId,
        needUpload: actionOptions.upload,
        needClose: actionOptions.close,
        needRelease: actionOptions.release,
        needDrop: actionOptions.dropIfFailure
    };
    // need to checksum
    if (handlerState.needChecksum) {
        (0, logging_1.startGroup)('Checksums');
        (0, logging_1.logInfo)(`Generation with config ${(0, util_1.inspect)(actionOptions.generateChecksumsConfig)}`);
        await (0, utils_1.generateChecksumFiles)(actionOptions.dir, actionOptions.generateChecksumsConfig);
        (0, logging_1.endGroup)();
    }
    // if there's a need to create a repo
    if (handlerState.needCreate) {
        (0, logging_1.startGroup)('Staging Repo Create');
        const stagedRepositoryId = await (0, nexus_utils_1.createStagingRepo)(nexusClient, actionOptions);
        handlerState.stagingRepoId = stagedRepositoryId;
        (0, logging_1.logInfo)(`Created repo ${stagedRepositoryId}`);
        (0, logging_1.endGroup)();
    }
    // need to upload files
    if (handlerState.needUpload && handlerState.stagingRepoId) {
        (0, logging_1.startGroup)('File Upload');
        await (0, nexus_utils_1.uploadFiles)(nexusClient, actionOptions.dir, handlerState.stagingRepoId, actionOptions.uploadParallel);
        (0, logging_1.endGroup)();
    }
    // need to close
    if (handlerState.needClose && handlerState.stagingRepoId) {
        (0, logging_1.startGroup)('Staging Repo Close');
        await (0, nexus_utils_1.closeStagingRepo)(nexusClient, actionOptions, handlerState.stagingRepoId);
        (0, logging_1.logInfo)(`Closed repo ${handlerState.stagingRepoId}`);
        (0, logging_1.logInfo)(`Waiting repo ${handlerState.stagingRepoId} state closed`);
        try {
            await (0, nexus_utils_1.waitRepoState)(nexusClient, handlerState.stagingRepoId, 'closed', actionOptions.closeTimeout);
        }
        catch (error) {
            await logActivity(nexusClient, handlerState.stagingRepoId);
            if (handlerState.needDrop) {
                await dropRepo(nexusClient, handlerState.stagingRepoId);
            }
            throw error;
        }
        (0, logging_1.endGroup)();
    }
    // need to release
    if (handlerState.needRelease && handlerState.stagingRepoId) {
        (0, logging_1.startGroup)('Staging Repo Release');
        try {
            await (0, nexus_utils_1.releaseStagingRepo)(nexusClient, actionOptions, handlerState.stagingRepoId);
        }
        catch (error) {
            await logActivity(nexusClient, handlerState.stagingRepoId);
            throw error;
        }
        (0, logging_1.logInfo)(`Released repo ${handlerState.stagingRepoId}`);
        (0, logging_1.logInfo)(`Waiting repo ${handlerState.stagingRepoId} state released`);
        try {
            await (0, nexus_utils_1.waitRepoState)(nexusClient, handlerState.stagingRepoId, 'released', actionOptions.releaseTimeout);
        }
        catch (error) {
            await logActivity(nexusClient, handlerState.stagingRepoId);
            throw error;
        }
        (0, logging_1.endGroup)();
    }
}
exports.handle = handle;
async function logActivity(nexusClient, stagingRepoId) {
    try {
        const activities = await (0, nexus_utils_1.stagingRepositoryActivity)(nexusClient, stagingRepoId);
        (0, logging_1.logInfo)(`Repo activities ${(0, util_1.inspect)(activities, false, 10)}`);
    }
    catch (error) { }
}
async function dropRepo(nexusClient, stagingRepoId) {
    try {
        (0, logging_1.logInfo)(`Dropping repo ${stagingRepoId}`);
        await (0, nexus_utils_1.dropStagingRepo)(nexusClient, stagingRepoId);
    }
    catch (error) { }
}
//# sourceMappingURL=handler.js.map