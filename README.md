# Zbar-Android  

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/ChenZeFengHi/material-palette)  [![Material Design](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/ChenZeFengHi/material-palette)

1. 使用Zbar实现相机扫描QR/Bar，采用了更高效的算法 ZBar扫描速度相比Zxing更快

2. 使用Zxing实现生成QR/Bar图片，这方面Zxing做的更好一些

结合ZBar的so库以及Zxing的lib库完美的实现了常用的二维码/条形码开发

最后还定义了 [BarLayerView.java](https://github.com/ChenZeFengHi/zbar-android/blob/master/app/src/main/java/com/zbar/code/camera/view/BarLayerView.java)，轻松帮你实现QR、Bar扫描动画以及它们切换时的过渡动画