const { app, BrowserWindow, dialog } = require("electron");
const path = require("path");
const { spawn } = require('node:child_process');

const plutos = spawn('java', ['-jar', 'plutos.jar']);

plutos.on('close', (code) => {
    app.exit(0)
})

await new Promise(resolve => setTimeout(resolve, 2500))

function createWindow() {
    const win = new BrowserWindow({
        width: 800,
        height: 600,
        webPreferences: {
            preload: path.join(__dirname, "preload.js"),
        },
    });
    win.loadFile("dist/index.html");
}
app.whenReady().then(() => {
    createWindow();

    app.on("activate", () => {
        if (BrowserWindow.getAllWindows().length === 0) {
            createWindow();
        }
    });
});

app.on('before-quit', e => {
    dialog.showMessageBoxSync({ message: JSON.stringify(e) })
})

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') {
        app.quit()
    }
})
