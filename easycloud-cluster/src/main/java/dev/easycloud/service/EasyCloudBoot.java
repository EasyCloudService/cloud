package dev.easycloud.service;

public final class EasyCloudBoot {

    public static void main(String[] args) {
        var cluster = new EasyCloudCluster();
        cluster.load();
        cluster.run();
    }
}
