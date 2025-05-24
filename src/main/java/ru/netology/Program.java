package ru.netology;

import ru.netology.setting.CommonSetting;
import ru.netology.setting.HandlerShaper;
import ru.netology.setting.ValidPathsStorage;

public class Program {
    public static void main(String[] args) {
        Server server = new Server();

        var handlerShaper = new HandlerShaper(ValidPathsStorage.getInstance());
        for (String path : ValidPathsStorage.getInstance().getValidPaths()) {
            server.addHandler(RequestMethods.GET.get(), path, handlerShaper.getHandler(RequestMethods.GET));
            server.addHandler(RequestMethods.POST.get(), path, handlerShaper.getHandler(RequestMethods.POST));
        }
        server.listen(CommonSetting.SERVER_PORT);
    }
}
