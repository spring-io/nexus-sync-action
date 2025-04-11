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
exports.Nexus2Client = void 0;
const fs_1 = __importDefault(require("fs"));
const https = __importStar(require("https"));
const axios_1 = __importDefault(require("axios"));
const axios_retry_1 = __importDefault(require("axios-retry"));
const logging_1 = require("./logging");
/**
 * Client for nexus 2.x proving common operations.
 */
class Nexus2Client {
    nexusServer;
    instance;
    constructor(nexusServer) {
        this.nexusServer = nexusServer;
        const config = {
            timeout: nexusServer.timeout,
            maxBodyLength: Infinity,
            maxContentLength: Infinity,
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json',
                Authorization: `Bearer ${nexusServer.token}`
            },
            httpsAgent: new https.Agent({})
        };
        if (nexusServer.username && nexusServer.password) {
            config.headers.Authorization = `Basic ${Buffer.from(`${nexusServer.username}:${nexusServer.password}`).toString('base64')}`;
        }
        this.instance = axios_1.default.create(config);
        (0, axios_retry_1.default)(this.instance, {
            onRetry: (retryCount, error) => {
                (0, logging_1.logInfo)(`Retry request count=${retryCount} status=${error.response?.status} path=${error.request?.path}`);
            }
        });
    }
    /**
     * Get a matching staging profile id from a staging profile name.
     *
     * @param stagingProfileName
     */
    getStagingProfileId(stagingProfileName) {
        return new Promise(async (resolve, reject) => {
            this.instance
                .get(`${this.nexusServer.url}/service/local/staging/profiles`)
                .then(r => {
                const stagingProfiles = r.data;
                const profile = stagingProfiles.data.find(p => p.name === stagingProfileName);
                if (profile) {
                    resolve(profile.id);
                }
                else {
                    reject('No profile found');
                }
            })
                .catch(e => {
                reject(e);
            });
        });
    }
    /**
     * Create a new staging repo were we can upload files. Takes stagingProfileId and PromoteStartRequest as arguments
     * and returns PromoteStartResponse as a promise.
     *
     * @param stagingProfileId the staging profile id
     * @param data promote start request
     */
    createStagingRepo(stagingProfileId, data) {
        return new Promise(async (resolve, reject) => {
            this.instance
                .post(`${this.nexusServer.url}/service/local/staging/profiles/${stagingProfileId}/start`, data)
                .then(r => {
                resolve(r.data);
            })
                .catch(e => {
                reject(e);
            });
        });
    }
    /**
     * Attempt to close a staging repo and returns PromoteFinishResponse as a Promise.
     *
     * @param stagingProfileId the staging profile id
     * @param data promote finish request
     */
    closeStagingRepo(stagingProfileId, data) {
        return new Promise(async (resolve, reject) => {
            this.instance
                .post(`${this.nexusServer.url}/service/local/staging/profiles/${stagingProfileId}/finish`, data)
                .then(r => {
                resolve(r.data);
            })
                .catch(e => {
                reject(e);
            });
        });
    }
    /**
     * Promote a staging repo and returns PromotePromoteResponse as a Promise.
     * We retry request 16 times with increasing delay to help how
     * nexus may return 500 after successfull close for next minute or so.
     *
     * @param data promote promote request
     */
    bulkPromoteStagingRepos(data) {
        return new Promise(async (resolve, reject) => {
            this.instance
                .post(`${this.nexusServer.url}/service/local/staging/bulk/promote`, data, {
                'axios-retry': {
                    retries: 10,
                    retryDelay: retryCount => {
                        return retryCount * 1000;
                    },
                    retryCondition(error) {
                        if (error.request.method === 'POST') {
                            return true;
                        }
                        return false;
                    }
                }
            })
                .then(r => {
                resolve(r.data);
            })
                .catch(e => {
                reject(e);
            });
        });
    }
    /**
     * Attempt to drop a staging repo and returns PromoteDropResponse as a Promise.
     *
     * @param stagingProfileId the staging profile id
     * @param data propote dro request
     */
    dropStagingRepo(stagingProfileId, data) {
        return new Promise(async (resolve, reject) => {
            this.instance
                .post(`${this.nexusServer.url}/service/local/staging/profiles/${stagingProfileId}/drop`, data)
                .then(r => {
                resolve(r.data);
            })
                .catch(e => {
                reject(e);
            });
        });
    }
    /**
     * Deploy a file into a staging repo and return Promise as a completion.
     *
     * @param uploadFile file to upload
     * @param repositoryId the repository id
     */
    deployByRepository(uploadFile, repositoryId) {
        (0, logging_1.logInfo)(`Upload for file ${uploadFile.path} and repo ${repositoryId}`);
        (0, logging_1.logDebug)(`File ${uploadFile.path} repositoryId=${repositoryId} group=${uploadFile.group} name=${uploadFile.name}`);
        return new Promise(async (resolve, reject) => {
            const stream = fs_1.default.createReadStream(uploadFile.path);
            (0, logging_1.logInfo)(`Handling file ${uploadFile.path}`);
            this.instance
                .put(`${this.nexusServer.url}/service/local/staging/deployByRepositoryId/${repositoryId}/${uploadFile.group}/${uploadFile.name}`, stream, {
                headers: {
                    'Content-Type': 'application/octet-stream',
                    Pragma: 'no-cache',
                    'Cache-Control': 'no-cache'
                }
            })
                .then(r => {
                (0, logging_1.logInfo)(`OK ${uploadFile.path}`);
                resolve();
            })
                .catch(e => {
                (0, logging_1.logWarn)(`ERROR ${uploadFile.path}`);
                reject(e);
            });
        });
    }
    /**
     * Get info about a repository. Mostly used by tracking repository states.
     *
     * @param repositoryIdKey
     */
    stagingRepository(repositoryIdKey) {
        return new Promise(async (resolve, reject) => {
            this.instance
                .get(`${this.nexusServer.url}/service/local/staging/repository/${repositoryIdKey}`)
                .then(r => {
                resolve(r.data);
            })
                .catch(e => {
                reject(e);
            });
        });
    }
    /**
     * Get info about a repository. Mostly used by tracking repository states.
     *
     * @param repositoryIdKey
     */
    stagingRepositoryActivity(repositoryIdKey) {
        return new Promise(async (resolve, reject) => {
            this.instance
                .get(`${this.nexusServer.url}/service/local/staging/repository/${repositoryIdKey}/activity`)
                .then(r => {
                resolve(r.data);
            })
                .catch(e => {
                reject(e);
            });
        });
    }
}
exports.Nexus2Client = Nexus2Client;
//# sourceMappingURL=nexus2-client.js.map