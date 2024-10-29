package dev.easycloud.service.setup.resources;

import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public final class SetupServiceResult {
    private final Map<SetupData<?>, Object> answers;

    public <T> T result(String id, Class<T> clazz) {
        var answer = this.answers.entrySet().stream()
                .filter(it -> it.getKey().id().equals(id))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);

        if(clazz.isEnum()) return (T) Enum.valueOf((Class<? extends Enum>) clazz, String.valueOf(answer));
        if(clazz == String.class) return (T) String.valueOf(answer);
        if(clazz == Integer.class) return (T) Integer.valueOf(String.valueOf(answer));

        throw new RuntimeException("Unknown class type: " + clazz);
    }
}
