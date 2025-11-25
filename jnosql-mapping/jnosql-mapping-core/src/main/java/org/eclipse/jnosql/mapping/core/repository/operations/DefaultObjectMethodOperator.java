package org.eclipse.jnosql.mapping.core.repository.operations;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jnosql.mapping.core.repository.ObjectMethodOperator;

import java.lang.reflect.Method;

@ApplicationScoped
class DefaultObjectMethodOperator implements ObjectMethodOperator {
    @Override
    public Object invokeObjectMethod(Object proxy, Method method, Object[] params) throws Exception {
        return method.invoke(this, params);
    }
}
