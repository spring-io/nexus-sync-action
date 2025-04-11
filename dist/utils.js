"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.delayPromise = exports.numberValue = exports.generateChecksumFiles = exports.createCheckSums = exports.findFiles = void 0;
const fs_1 = __importDefault(require("fs"));
const glob_1 = require("glob");
const path_1 = __importDefault(require("path"));
const crypto_1 = __importDefault(require("crypto"));
const logging_1 = require("./logging");
/**
 * Find files from a base directory to get uploaded by automatically
 * giving correct structure ready for api requests.
 *
 * @param baseDir base directory for uploaded files structure
 */
async function findFiles(baseDir) {
    return new Promise((resolve, reject) => {
        return (0, glob_1.glob)(baseDir + '/**/*').then(files => {
            let found = [];
            files.forEach(f => {
                const stat = fs_1.default.lstatSync(f);
                if (stat.isFile()) {
                    found.push({
                        path: f,
                        name: path_1.default.basename(f),
                        group: path_1.default.relative(baseDir, path_1.default.dirname(f))
                    });
                    (0, logging_1.logInfo)(`Found ${f}`);
                }
            });
            resolve(found);
        }, error => {
            reject(error);
            return;
        });
    });
}
exports.findFiles = findFiles;
async function createCheckSums(path, config) {
    return new Promise((resolve, reject) => {
        const stat = fs_1.default.lstatSync(path);
        if (stat.isFile()) {
            const hashes = new Map();
            config.forEach(c => {
                const hash = crypto_1.default.createHash(c.type);
                hashes.set(c.type, hash);
            });
            const stream = fs_1.default.createReadStream(path);
            stream.on('data', data => {
                for (const hash of hashes.values()) {
                    hash.update(data);
                    // hash.update(data, 'utf8');
                }
            });
            stream.on('end', () => {
                const digests = new Map();
                hashes.forEach((hash, type) => {
                    const digest = hash.digest('hex');
                    digests.set(type, digest);
                });
                resolve(digests);
            });
        }
        else {
            reject(`${path} is not a file`);
        }
    });
}
exports.createCheckSums = createCheckSums;
async function generateChecksumFiles(baseDir, config) {
    return new Promise((resolve, reject) => {
        return (0, glob_1.glob)(baseDir + '/**/!(*.asc)').then(files => {
            let all = [];
            files.forEach(path => {
                const stat = fs_1.default.lstatSync(path);
                if (stat.isFile()) {
                    const p = createCheckSums(path, config).then(res => {
                        res.forEach((digest, type) => {
                            const dpath = `${path}.${type}`;
                            (0, logging_1.logInfo)(`Writing ${dpath}`);
                            fs_1.default.writeFileSync(dpath, digest);
                        });
                    });
                    all.push(p);
                }
            });
            resolve(Promise.all(all).then());
        }, error => {
            reject(error);
            return;
        });
    });
}
exports.generateChecksumFiles = generateChecksumFiles;
/**
 * Returns value or default and handles if value is "falsy".
 */
function numberValue(value, defaultValue) {
    let v;
    if (typeof value === 'string') {
        if (value.length > 0) {
            v = parseInt(value, 10);
            if (isNaN(v)) {
                throw new Error(`Can't parse '${value}' as number`);
            }
        }
    }
    else {
        v = value;
    }
    if (v === 0) {
        return v;
    }
    return v || defaultValue;
}
exports.numberValue = numberValue;
/**
 * Returns a simple promise with delay timeout.
 */
function delayPromise(millis) {
    return new Promise(resolve => setTimeout(resolve, millis));
}
exports.delayPromise = delayPromise;
//# sourceMappingURL=utils.js.map