package org.springframework.content.commons.repository.factory;

import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.BeforeEach;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.Context;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.Describe;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.It;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.content.commons.repository.ContentRepositoryExtension;
import org.springframework.content.commons.repository.ContentRepositoryInvoker;
import org.springframework.content.commons.repository.ContentStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.ReflectionUtils;

import com.github.paulcwarren.ginkgo4j.Ginkgo4jConfiguration;
import com.github.paulcwarren.ginkgo4j.Ginkgo4jSpringRunner;

@RunWith(Ginkgo4jSpringRunner.class )
@Ginkgo4jConfiguration(threads=1)
@ContextConfiguration(classes = StoreExtensionTest.TestConfiguration.class)
public class StoreExtensionTest {

    @Autowired
    private TestContentRepository repo;

    @Autowired
    private TestContentRepositoryExtension testExtensionService;

    // mocks/spys
    private TestContentRepositoryExtension spy;

    {
        Describe("AbstractContentStoreFactoryBean", () -> {
            Context("given a repository extension bean", () -> {
                Context("given an extension method is invoked on the proxy", () -> {
                    BeforeEach(() -> {
                        repo.someMethod();
                    });
                    It("should forward that method onto the extension", () -> {
                        verify(testExtensionService).someMethod();
                    });
                });
            });
        });
    }

    @Configuration
    public static class TestConfiguration {

        @Bean
        public TestContentRepositoryExtension textExtensionService() {
            return spy(new TestContentRepositoryExtension());
        }

        @Bean
        public Class<? extends ContentStore<Object, Serializable>> contentStoreInterface() {
            return TestContentRepository.class;
        }

        @Bean
        public AbstractContentStoreFactoryBean contentStoreFactory() {
            return new TestContentStoreFactory();
        }
    }

    public static class TestContentStoreFactory extends AbstractContentStoreFactoryBean {
        @Override
        protected Object getContentStoreImpl() {
            return new TestConfigStoreImpl();
        }
    }

    public static class TestConfigStoreImpl {

    }

    public interface TestContentRepository extends ContentStore<Object, Serializable>, TestExtensionService {
    }

    public interface TestExtensionService {
        Object someMethod();
    }

    public static class TestContentRepositoryExtension implements TestExtensionService, ContentRepositoryExtension {

        @Override
        public Object someMethod() {
            return null;
        }

        @Override
        public Set<Method> getMethods() {
            Set<Method> methods = new HashSet<>();
            try {
                methods.add(TestExtensionService.class.getMethod("someMethod",new Class<?>[]{}));
            } catch (NoSuchMethodException e) {
                Assert.fail();
            }
            return methods;
        }

        @Override
        public Object invoke(MethodInvocation invocation, ContentRepositoryInvoker invoker) {
            return ReflectionUtils.invokeMethod(invocation.getMethod(), this, null);
        }
    }

    @Test
    public void noop() {}
}
