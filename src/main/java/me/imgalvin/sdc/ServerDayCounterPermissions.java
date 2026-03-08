package me.imgalvin.sdc;

import java.lang.reflect.Method;
import java.util.Objects;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permissions;

public final class ServerDayCounterPermissions {
    private static final String MESSAGE_PERMISSION = "serverdaycounter.command.message";
    private static final String PERMISSIONS_CLASS = "me.lucko.fabric.api.permissions.v0.Permissions";

    private static Method fabricPermissionsCheckMethod;
    private static boolean permissionsLookupComplete;

    private ServerDayCounterPermissions() {
    }

    public static boolean canManageMessages(CommandSourceStack source) {
        Boolean fabricPermissionsResult = checkWithFabricPermissionsApi(source);
        return Objects.requireNonNullElseGet(fabricPermissionsResult, () -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));
    }

    private static Boolean checkWithFabricPermissionsApi(CommandSourceStack source) {
        Method checkMethod = getFabricPermissionsCheckMethod();
        if (checkMethod == null || !checkMethod.getParameterTypes()[0].isInstance(source)) {
            return null;
        }

        try {
            Object result = checkMethod.invoke(null, source, MESSAGE_PERMISSION, 2);
            return result instanceof Boolean ? (Boolean) result : null;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private static Method getFabricPermissionsCheckMethod() {
        if (permissionsLookupComplete) {
            return fabricPermissionsCheckMethod;
        }

        permissionsLookupComplete = true;

        try {
            Class<?> permissionsClass = Class.forName(PERMISSIONS_CLASS);
            for (Method method : permissionsClass.getMethods()) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (!method.getName().equals("check")) {
                    continue;
                }
                if (parameterTypes.length != 3) {
                    continue;
                }
                if (parameterTypes[1] != String.class || parameterTypes[2] != int.class) {
                    continue;
                }
                if (method.getReturnType() != boolean.class) {
                    continue;
                }

                fabricPermissionsCheckMethod = method;
                break;
            }
        } catch (ClassNotFoundException | LinkageError ignored) {
            fabricPermissionsCheckMethod = null;
        }

        return fabricPermissionsCheckMethod;
    }
}
