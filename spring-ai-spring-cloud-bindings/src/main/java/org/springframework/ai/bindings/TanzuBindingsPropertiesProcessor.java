/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ai.bindings;

import org.springframework.cloud.bindings.Binding;
import org.springframework.cloud.bindings.Bindings;
import org.springframework.cloud.bindings.boot.BindingsPropertiesProcessor;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Map;

/**
 * An implementation of {@link BindingsPropertiesProcessor} that detects {@link Binding}s
 * of type: {@value TYPE}.
 *
 * @author Stuart Charlton
 */
public class TanzuBindingsPropertiesProcessor implements BindingsPropertiesProcessor {

	/**
	 * The {@link Binding} type that this processor is interested in: {@value}.
	 **/
	public static final String TYPE = "genai";

	@Override
	public void process(Environment environment, Bindings bindings, Map<String, Object> properties) {
		if (!BindingsValidator.isTypeEnabled(environment, TYPE)) {
			return;
		}

		bindings.filterBindings(TYPE).forEach(binding -> {
			if (binding.getSecret().get("model-capabilities") != null) {
				String[] capabilities = binding.getSecret().get("model-capabilities").trim().split("\\s*,\\s*");
				if (Arrays.stream(capabilities).anyMatch("chat"::equals)) {
					properties.put("spring.ai.openai.chat.api-key", binding.getSecret().get("api-key"));
					properties.put("spring.ai.openai.chat.base-url", binding.getSecret().get("uri"));
					properties.put("spring.ai.openai.chat.options.model", binding.getSecret().get("model-name"));
				}
				if (Arrays.stream(capabilities).anyMatch("embedding"::equals)) {
					properties.put("spring.ai.openai.embedding.api-key", binding.getSecret().get("api-key"));
					properties.put("spring.ai.openai.embedding.base-url", binding.getSecret().get("uri"));
					properties.put("spring.ai.openai.embedding.options.model", binding.getSecret().get("model-name"));
				}
			}
		});
	}

}