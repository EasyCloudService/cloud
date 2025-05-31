# EasyCloudService

## 📥 [Download](https://github.com/EasyCloudService/cloud/releases)

## ❓ FAQ

### 🔹 I want AutoUpdates, what can I do?
To enable them, open the `start-script` (`start.bat` or `start.sh`) and add the following argument \
`-Dauto.updates=true` to the end. It should look like this: \
`java -Xms512M -Xmx512M -jar easycloud-loader.jar -Dauto.updates=true`

### 🔹 How can I change the language?
You can change the language by editing the `local/config.json` file.\
Currently, the following languages are supported: `en,de`

### 🔹 How can I use the API?
To use the API, you need to add following dependency to your `build.gradle.kts`:
```kotlin
dependencies {
    implementation("com.github.EasyCloudService.cloud:easycloud-plugin:[current_version]")
}
```
Then you can use the API like this:
```java
EasyCloudService.instance().serviceProvider().current();
```

### 🔹 How do I set up a group?
```java
group setup
```

### 🔹 How do I set up a screen?
```java
service screen [name]
```

### 🔹 Is there an auto-updater?
Not yet.

### 🔹 I found a bug. How can I report it?
Please open an issue on [GitHub](https://github.com/EasyCloudService/cloud/issues).

### 🔹 Can I use EasyCloudService for free?
Yes, EasyCloudService is completely free to use.

### 🔹 Can I download EasyCloudService?
Yes, but the cloud service is still under development.  
Check out the latest [Releases](https://github.com/EasyCloudService/cloud/releases).
