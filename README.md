# ZBar Android  

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/ChenZeFengHi/material-palette)  [![Material Design](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/ChenZeFengHi/material-palette)

1. 使用Zbar实现相机扫描QR/Bar，采用了更高效的算法 ZBar扫描速度相比Zxing更快

2. 使用Zxing实现生成QR/Bar图片，这方面Zxing做的更好一些

结合ZBar的so库以及Zxing的lib库完美的实现了常用的二维码/条形码开发

最后还定义了 [BarLayerView.java](https://github.com/ChenZeFengHi/zbar-android/blob/master/app/src/main/java/com/zbar/code/camera/view/BarLayerView.java)，轻松帮你实现QR、Bar扫描动画以及它们切换时的过渡动画





**如需要更新so、jar 可自行编译**

>* ZBar的编译项目： https://github.com/ChenZeFengHi/zbar-build

---

flag 敬请期待Camera2 API版本...



## License

```java
/*
 * Copyright (C) 2019 Chen Ze Feng <chenzefenghi@gmail.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
```

