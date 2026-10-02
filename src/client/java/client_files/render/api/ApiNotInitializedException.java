package client_files.render.api;

public class ApiNotInitializedException extends IllegalStateException {
    public ApiNotInitializedException() {
        super("RenderAPI has not been initialized yet. Call RenderAPI.init(...) first.");
    }
}
