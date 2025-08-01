@file:OptIn(ExperimentalForeignApi::class)

import kotlinx.cinterop.*
import platform.windows.*

fun main() {
    val procName = "veyon-worker.exe"
    val procID = getProcessID(procName)

    if (procID == 0u) {
        error("id not found for process with name: $procName")
    }

    println("Killing $procName with pid: $procID")

    val permsFlag: DWORD = (PROCESS_TERMINATE).toUInt()

    val procHandle: HANDLE? = OpenProcess(permsFlag, 0, procID)
    TerminateProcess(procHandle, 1u)
    CloseHandle(procHandle)
}

private fun getProcessID(processName: String): UInt = memScoped {
    val snapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS.toUInt(),0U)
    val processEntry = alloc<PROCESSENTRY32W>().apply {
        dwSize = sizeOf<PROCESSENTRY32W>().convert()
    }

    if (Process32FirstW(snapshot,processEntry.ptr) == 0) {
        CloseHandle(snapshot)
        return 0U
    }

    do {
        val name = processEntry.szExeFile.toKString()
        if (processName == name) {
            CloseHandle(snapshot)
            return processEntry.th32ProcessID
        }
    } while (Process32NextW(snapshot, processEntry.ptr) != 0)

    CloseHandle(snapshot)
    return 0U
}

