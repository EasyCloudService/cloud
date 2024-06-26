package net.easycloud.base.rest;

import net.easycloud.api.utils.file.FileHelper;
import net.easycloud.base.Base;

import static spark.Spark.get;

public final class RestAPI {
    public RestAPI() {
        get("/valid", (req, res) -> checkIfValid(req.queryParams("adminKey")));
        get("/users/size", (req, res) -> {
            if(!checkIfValid(req.queryParams("adminKey"))) return "ERROR";
            return Base.instance().userProvider().getUsers().size();
        });
        get("/users", (req, res) -> {
            if(!checkIfValid(req.queryParams("adminKey"))) return "ERROR";
            return FileHelper.GSON.toJson(Base.instance().userProvider().getUsers());
        });
        get("/users/online/size", (req, res) -> {
            if(!checkIfValid(req.queryParams("adminKey"))) return "ERROR";
            return Base.instance().userProvider().getOnlineUsers().size();
        });
        get("/users/online", (req, res) -> {
            if(!checkIfValid(req.queryParams("adminKey"))) return "ERROR";
            return FileHelper.GSON.toJson(Base.instance().userProvider().getOnlineUsers());
        });
        get("/groups/size", (req, res) -> {
            if(!checkIfValid(req.queryParams("adminKey"))) return "ERROR";
            return Base.instance().groupProvider().getRepository().query().find().size();
        });
        get("/groups", (req, res) -> {
            if(!checkIfValid(req.queryParams("adminKey"))) return "ERROR";
            return FileHelper.GSON.toJson(Base.instance().groupProvider().getRepository().query().find());
        });

        Base.instance().logger().log("§7RestAPI is listening on following port: 4567");
    }

    private boolean checkIfValid(String key) {
        return Base.instance().configuration().adminKey().equals(key);
    }
}
