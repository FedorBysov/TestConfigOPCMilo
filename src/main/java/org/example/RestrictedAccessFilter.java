package org.example;

import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.server.Session;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilter;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilterContext;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;


//Этот класс реализует AttributeFilter интерфейс, предоставляемый OPC UA SDK.
// Это позволяет классу перехватывать запросы атрибутов данных и
// потенциально ограничивать доступ на основе определенных правил.
public class RestrictedAccessFilter implements AttributeFilter {

    //Определяет уровни доступа
    private static final Set<AccessLevel> INTERNAL_ACCESS = AccessLevel.READ_WRITE;

    //
    private final Function<Object, Set<AccessLevel>> accessLevelsFn;


    // Функция отвечает за определение уровней доступа для конкретного пользователя
    public RestrictedAccessFilter(Function<Object, Set<AccessLevel>> accessLevelsFn) {
        this.accessLevelsFn = accessLevelsFn;
    }

    @Override
    public Object getAttribute(AttributeFilterContext.GetAttributeContext ctx, AttributeId attributeId) {
        if (attributeId == AttributeId.UserAccessLevel) {
            Optional<Object> identity = ctx.getSession().map(Session::getIdentityObject);

            Set<AccessLevel> accessLevels = identity.map(accessLevelsFn).orElse(INTERNAL_ACCESS);

            return AccessLevel.toValue(accessLevels);
        } else {
            return ctx.getAttribute(attributeId);
        }
    }

}
