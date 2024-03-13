import * as core from '@actions/core'

export function logWarn(message: string) {
  core.warning(message)
}

export function logError(message: string) {
  core.error(message)
}

export function logInfo(message: string) {
  core.info(message)
}

export function logDebug(message: string) {
  core.debug(message)
}

export function startGroup(group: string) {
  core.startGroup(group)
}

export function endGroup() {
  core.endGroup()
}
