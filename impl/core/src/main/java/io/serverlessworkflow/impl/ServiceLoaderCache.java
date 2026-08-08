/*
 * Copyright 2020-Present The Serverless Workflow Specification Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.serverlessworkflow.impl;

import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Utility class for caching ServiceLoader results to avoid repeated classpath scanning.
 * ServiceLoader scanning is expensive as it iterates through all classpath entries and
 * instantiates service provider classes. This cache ensures services are loaded once and
 * reused across the application lifecycle.
 */
public class ServiceLoaderCache {

  private static final ConcurrentMap<Class<?>, List<?>> cache = new ConcurrentHashMap<>();

  private ServiceLoaderCache() {}

  /**
   * Loads and caches services of the specified class using ServiceLoader. Subsequent calls
   * return the cached result without rescanning the classpath.
   *
   * @param <T> the service type
   * @param serviceClass the service class to load
   * @return a sorted, immutable list of loaded service instances
   */
  @SuppressWarnings("unchecked")
  public static <T extends ServicePriority> List<T> loadServices(Class<T> serviceClass) {
    return (List<T>)
        cache.computeIfAbsent(
            serviceClass,
            k ->
                ServiceLoader.load(k).stream()
                    .map(Provider::get)
                    .sorted()
                    .toList());
  }

  /**
   * Loads and caches services of the specified class without sorting. Use this when ordering
   * is not required to save computation.
   *
   * @param <T> the service type
   * @param serviceClass the service class to load
   * @return an immutable list of loaded service instances in discovery order
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> loadServicesUnsorted(Class<T> serviceClass) {
    return (List<T>)
        cache.computeIfAbsent(
            serviceClass,
            k -> ServiceLoader.load(k).stream().map(Provider::get).toList());
  }

  /** Clears the service cache. Useful for testing or dynamic reloading scenarios. */
  public static void clear() {
    cache.clear();
  }

  /** Returns the current cache size for diagnostics. */
  public static int cacheSize() {
    return cache.size();
  }
}
