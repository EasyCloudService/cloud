package dev.easycloud.service.setup.resources;


import java.util.Collection;

public record SetupData<T>(String id, String question, Collection<T> possible) {
}
