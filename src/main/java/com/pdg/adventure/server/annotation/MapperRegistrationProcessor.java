package com.pdg.adventure.server.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.server.support.MapperSupporter;

/**
 * Spring BeanPostProcessor that automatically registers mappers annotated with @RegisterMapper.
 * This eliminates the need for manual registration in @PostConstruct methods.
 */
@Component
@Order(1000) // Process after MapperSupporter is fully initialized
public class MapperRegistrationProcessor implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(MapperRegistrationProcessor.class);

    private final MapperSupporter mapperSupporter;
    private final List<PendingRegistration> pendingRegistrations = new ArrayList<>();

    public MapperRegistrationProcessor(MapperSupporter mapperSupporter) {
        this.mapperSupporter = mapperSupporter;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        RegisterMapper annotation = AnnotationUtils.findAnnotation(bean.getClass(), RegisterMapper.class);

        if (annotation != null && bean instanceof Mapper<?, ?> mapper) {
            pendingRegistrations.add(new PendingRegistration(
                    annotation.dataObjectClass(),
                    annotation.businessObjectClass(),
                    mapper,
                    annotation.priority(),
                    beanName
            ));

            logger.debug("Queued mapper {} for registration with priority {}", beanName, annotation.priority());
        }

        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // Check if this is the MapperSupporter being fully initialized
        if (bean instanceof MapperSupporter && "mapperSupporter".equals(beanName)) {
            registerAllPendingMappers();
        }

        return bean;
    }

    private void registerAllPendingMappers() {
        if (pendingRegistrations.isEmpty()) {
            return;
        }

        // Sort by priority (lower numbers first)
        pendingRegistrations.sort(Comparator.comparingInt(reg -> reg.priority));

        logger.info("Registering {} mappers automatically...", pendingRegistrations.size());

        for (PendingRegistration registration : pendingRegistrations) {
            try {
                mapperSupporter.registerMapper(
                        registration.dataObjectClass,
                        registration.businessObjectClass,
                        registration.mapper
                );

                logger.debug("Registered mapper {} for {} -> {}",
                             registration.beanName,
                             registration.dataObjectClass.getSimpleName(),
                             registration.businessObjectClass.getSimpleName()
                );

            } catch (Exception e) {
                logger.error("Failed to register mapper {}: {}", registration.beanName, e.getMessage(), e);
            }
        }

        pendingRegistrations.clear();
        logger.info("Completed automatic mapper registration");
    }

    private record PendingRegistration(Class<?> dataObjectClass, Class<?> businessObjectClass, Mapper<?, ?> mapper,
                                       int priority, String beanName) {
    }
}
