> ⚠️ **Disclaimer:**\
> This is **not a final version** of EasyCloudService. Bugs **may occur**, and some features are **still missing**.
> Everything is subject to change.
> If you encounter a **bug**, please open an [issue](https://github.com/EasyCloudService/cloud/issues) or contact us directly via Discord: [discord.gg/D5EKk9Cr2P](https://discord.gg/D5EKk9Cr2P)  
> If you need a feature or have suggestions, **reach out to us!** 💬


# 🚀 EasyCloudService
> **The next-generation cloud management platform that makes scaling effortless**

<div align="center">

[![Download](https://img.shields.io/github/downloads/EasyCloudService/cloud/total?style=for-the-badge&logo=github&color=2ea043)](https://github.com/EasyCloudService/cloud/releases)
[![Version](https://img.shields.io/github/v/release/EasyCloudService/cloud?style=for-the-badge&logo=semver&color=blue)](https://github.com/EasyCloudService/cloud/releases)
[![Discord](https://img.shields.io/discord/1235237612931776512?label=Community&style=for-the-badge&logo=discord&color=7289da)](https://discord.gg/D5EKk9Cr2P)
[![Wiki](https://img.shields.io/badge/Docs-Wiki-4d7a97?style=for-the-badge&logo=gitbook)](https://github.com/EasyCloudService/cloud/wiki)

**[📥 Download](https://github.com/EasyCloudService/cloud/releases)** • **[📚 Documentation](https://github.com/EasyCloudService/cloud/wiki)** • **[💬 Discord](https://discord.gg/D5EKk9Cr2P)** • **[🐛 Issues](https://github.com/EasyCloudService/cloud/issues)**

</div>

---

Guidelines for using EasyCloudService can be found in the [GUIDELINES.md](GUIDELINES.md) file.\
If you are using EasyCloudService, you must follow these guidelines.

## ✨ Features

<table>
<tr>
<td width="50%">

### 🎯 **Core Features**
- **🔄 Auto-Updates** - Keep your services always up-to-date
- **🌍 Multi-Language** - English & German support
- **⚡ High Performance** - Optimized for speed and reliability
- **🔧 Easy Setup** - Get started in minutes, not hours
- **📊 Real-time Monitoring** - Track your services live

</td>
<td width="50%">

### 🛠️ **Developer Tools**
- **🔌 Plugin API** - Extend functionality easily
- **🖥️ CLI Interface** - Powerful command-line tools
- **📈 Analytics** - Built-in performance metrics
- **🔒 Security First** - Enterprise-grade security

</td>
</tr>
</table>

---

## 🚀 Quick Start

### Prerequisites
- ☕ Java 21 or higher
- 💾 At least 4GB RAM
- 🌐 Internet connection
- 🔮 All versions from 1.17 up to 1.21 are supported

### Installation

1. **Download the latest release**
   ```bash
   wget https://github.com/EasyCloudService/cloud/releases/latest/download/easycloud-loader.jar
   ```

2. **Run EasyCloudService**
   ```bash
   java -Xms512M -Xmx512M -jar easycloud-loader.jar
   ```

3. **🎉 That's it!** Your cloud service is now running!

---

## ⚙️ Configuration

**Supported Languages:**
- 🇺🇸 `en` - English
- 🇩🇪 `de` - German

---

## 🔌 API Integration

### Gradle Setup (Kotlin DSL)
```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    compileOnly("com.github.EasyCloudService.cloud:easycloud-api:[current_version]")
    compileOnly("com.github.EasyCloudService.cloud:easycloud-service:[current_version]")
}
```

### Maven Setup
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
<dependency>
    <groupId>com.github.EasyCloudService.cloud</groupId>
    <artifactId>easycloud-api</artifactId>
    <version>[current_version]</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>com.github.EasyCloudService.cloud</groupId>
    <artifactId>easycloud-service</artifactId>
    <version>[current_version]</version>
    <scope>provided</scope>
    <exclusions>
        <exclusion>
            <groupId>io.activej</groupId>
            <artifactId>activej</artifactId>
        </exclusion>
    </exclusions>
</dependency>
</dependencies>
```

### Usage Example
```java
// Get current service instance
ServiceProvider provider = EasyCloudService.instance().serviceProvider();
var service = provider.thisService();
```

---

## 🛠️ Commands

<table>
<tr>
<th>Command</th>
<th>Description</th>
<th>Example</th>
</tr>
<tr>
<td><code>group setup</code></td>
<td>Initialize a new service group</td>
<td><code>group setup</code></td>
</tr>
<tr>
<td><code>service screen [name]</code></td>
<td>Attach to service console</td>
<td><code>service screen Lobby-1</code></td>
</tr>
<tr>
<td><code>service start</code></td>
<td>Start a specific group</td>
<td><code>service start</code></td>
</tr>
<tr>
<td><code>service stop</code></td>
<td>Stop a specific service</td>
<td><code>service stop</code></td>
</tr>
</table>

---

## 📊 System Requirements

| Component   | Minimum | Recommended |
|-------------|---------|-------------|
| **Java**    | 21+     | 21+         |
| **RAM**     | 4GB     | 16GB+       |
| **Storage** | 5GB     | 25GB+       |
| **CPU**     | 2 Cores | 4+ Cores    |

---

## 🤝 Contributing

We love contributions! Here's how you can help:

1. **🍴 Fork** the repository
2. **🌿 Create** a feature branch
3. **💻 Commit** your changes
4. **📤 Push** to the branch
5. **🔄 Open** a Pull Request

### Development Setup
```bash
# Clone the repository
git clone https://github.com/EasyCloudService/cloud.git

# Navigate to project directory
cd cloud

# Then Build the jar from the loader module and run it
```

---

## 🐛 Bug Reports & Feature Requests

Found a bug? Have a great idea? We want to hear from you!

- 🐛 **Bug Reports**: [Create an Issue](https://github.com/EasyCloudService/cloud/issues/new?template=bug_report.md)
- 💡 **Feature Requests**: [Request a Feature](https://github.com/EasyCloudService/cloud/issues/new?template=feature_request.md)
- 💬 **Questions**: Join our [Discord Community](https://discord.gg/D5EKk9Cr2P)

---

## 📋 Roadmap

- [ ] 📊 **Web Dashboard** - Beautiful management interface
- [ ] 🔌 **Module "Marketplace"** - Community-driven modules
- [ ] ☁️ **Multi-Cloud Support** - Clustering

---

## 📄 License

This project is licensed under the Apache2 License - see the [LICENSE](LICENSE) file for details.

---

## 🌟 Support the Project

If EasyCloudService helps you, consider:

- ⭐ **Starring** this repository
- 🐛 **Reporting bugs** to help us improve
- 💬 **Spreading the word** in your community
- 🤝 **Contributing** code or documentation

---

<div align="center">

**Made with ❤️ by the EasyCloudService Team**

[![Contributors](https://img.shields.io/github/contributors/EasyCloudService/cloud?style=for-the-badge)](https://github.com/EasyCloudService/cloud/graphs/contributors)
[![Last Commit](https://img.shields.io/github/last-commit/EasyCloudService/cloud?style=for-the-badge)](https://github.com/EasyCloudService/cloud/commits)
[![Stars](https://img.shields.io/github/stars/EasyCloudService/cloud?style=for-the-badge)](https://github.com/EasyCloudService/cloud/stargazers)

**[⬆️ Back to Top](#-easycloudservice)**

</div>
