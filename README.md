# 我的伊甸园

我的伊甸园是一款跨平台浏览器应用，主要用于构建一个伊甸园, 和我喜欢的AI生活在一起。

<!-- PROJECT SHIELDS -->

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url]

<!-- PROJECT LOGO -->
<br />

<p align="center">
  <a href="https://github.com/jinny76/myeden/">
    <img src="resources/icon.png" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">我的伊甸园</h3>
  <p align="center">
    和你喜欢的AI一起生活, 没有其他人
    <br />
    <a href="https://github.com/jinny76/myeden"><strong>探索本项目的文档 »</strong></a>
    <br />
    <br />
    <a href="https://github.com/jinny76/myeden">查看Demo</a>
    ·
    <a href="https://github.com/jinny76/myeden/issues">报告Bug</a>
    ·
    <a href="https://github.com/jinny76/myeden/issues">提出新特性</a>
  </p>

</p>

和你喜欢的AI一起生活, 没有其他人

## 目录

- [用户教程]()
- [上手指南](#上手指南)
    - [开发前的配置要求](#开发前的配置要求)
    - [安装步骤](#安装步骤)
- [文件目录说明](#文件目录说明)
- [开发的架构](#开发的架构)
- [部署](#部署)
- [使用到的框架](#使用到的框架)
- [贡献者](#贡献者)
    - [如何参与开源项目](#如何参与开源项目)
- [版本控制](#版本控制)
- [作者](#作者)
- [鸣谢](#鸣谢)

### 上手指南

请将本项目克隆到本地机器。

###### 开发前的配置要求

1. Nodejs 18.x.x

###### **安装步骤**

### 安装

```bash
$ npm install
```

### 调试

```bash
$ npm run dev
```

### 打包

```bash
# Windows
$ npm run build:win

# MacOS
$ npm run build:mac

# Linux
$ npm run build:linux
```

### 文件目录说明

```
文件目录
├── package.json 项目配置文件
├── LICENSE.txt
├── README.md
├── /build/ 打包后的文件
├── /resources/ 资源
└── /src/ 源码
    ├── /main/ 主进程
    ├── /preload/ 预加载
    └── /renderer/ 渲染进程
```

### 部署

暂无

### 使用到的框架

- [Electron Vite](https://cn.electron-vite.org/)
- [Vditor](https://github.com/Vanessa219/vditor)

### 贡献者

请阅读**CONTRIBUTING.md** 查阅为该项目做出贡献的开发者。

#### 如何参与开源项目

贡献使开源社区成为一个学习、激励和创造的绝佳场所。你所作的任何贡献都是**非常感谢**的。

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### 版本控制

该项目使用Git进行版本管理。您可以在repository参看当前可用版本。

### 作者

太白雪霁

jinni@kingfishers.cn

Wechat:jinny76 &ensp; qq:147279

*您也可以在贡献者名单中参看所有参与该项目的开发者。*

### 版权说明

该项目签署了MIT 授权许可，详情请参阅 [LICENSE.txt](https://github.com/jinny76/myeden/blob/master/LICENSE.txt)

### 鸣谢

- [Electron Vite](https://cn.electron-vite.org/)
- [Vditor](https://github.com/Vanessa219/vditor)

<!-- links -->

[your-project-path]:jinny76/myeden

[contributors-shield]: https://img.shields.io/github/contributors/jinny76/myeden.svg?style=flat-square

[contributors-url]: https://github.com/jinny76/myeden/graphs/contributors

[forks-shield]: https://img.shields.io/github/forks/jinny76/myeden.svg?style=flat-square

[forks-url]: https://github.com/jinny76/myeden/network/members

[stars-shield]: https://img.shields.io/github/stars/jinny76/myeden.svg?style=flat-square

[stars-url]: https://github.com/jinny76/myeden/stargazers

[issues-shield]: https://img.shields.io/github/issues/jinny76/myeden.svg?style=flat-square

[issues-url]: https://img.shields.io/github/issues/jinny76/myeden.svg

[license-shield]: https://img.shields.io/github/license/jinny76/myeden.svg?style=flat-square

[license-url]: https://github.com/jinny76/myeden/blob/master/LICENSE.txt

[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=flat-square&logo=linkedin&colorB=555

[linkedin-url]: https://www.linkedin.com/in/jinni-kim-8903a836/
