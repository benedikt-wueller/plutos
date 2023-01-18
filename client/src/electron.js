const { app, BrowserWindow, dialog } = require("electron")
const path = require("path")
const { spawn } = require('node:child_process')

function createWindow() {
    const win = new BrowserWindow({
        width: 800,
        height: 600,
        webPreferences: {
            preload: path.join(__dirname, "preload.js"),
        },
    });
    win.loadFile("dist/index.html")
}

const plutosBackend = spawn('java', ['-jar', 'resources/plutos.jar'])
plutosBackend.on('close', (code) => {
    app.exit(0)
})

app.whenReady().then(() => {
    createWindow()

    app.on("activate", () => {
        if (BrowserWindow.getAllWindows().length === 0) {
            createWindow()
        }
    })
})

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') {
        app.quit()
    }
})
