/**
 *  Copyright 2016-2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.antheminc.oss.nimbus.domain.cmd.exec.internal;

import com.antheminc.oss.nimbus.context.BeanResolverStrategy;
import com.antheminc.oss.nimbus.domain.cmd.exec.AbstractCommandExecutor;
import com.antheminc.oss.nimbus.domain.cmd.exec.CommandExecution.Input;
import com.antheminc.oss.nimbus.domain.cmd.exec.CommandExecution.Output;
import com.antheminc.oss.nimbus.domain.cmd.exec.ExecutionContext;
import com.antheminc.oss.nimbus.domain.model.state.EntityState.Param;
import com.antheminc.oss.nimbus.domain.model.state.repo.ModelRepository;
import com.antheminc.oss.nimbus.support.EnableLoggingInterceptor;

/**
 * @author Soham Chakravarti
 *
 */
@EnableLoggingInterceptor
public class DefaultActionExecutorSave extends AbstractCommandExecutor<Boolean> {

	public DefaultActionExecutorSave(BeanResolverStrategy beanResolver) {
		super(beanResolver);
	}
	
	@Override
	protected Output<Boolean> executeInternal(Input input) {
		ExecutionContext eCtx = input.getContext();
		
		Param<Object> p = findParamByCommandOrThrowEx(eCtx);
		
		// find applicable Model Repositories
		RepoDatabaseResolution r = resolveByRepoDatabase(p.getRootDomain().getConfig());
		
		// save core first
		if(r.isMapsToPersistable()) {
			ModelRepository mapsToRepo = getRepositoryFactory().get(r.getMapsTo());
			Param<?> mapsToParam = p.getRootDomain().getAssociatedParam().findIfMapped().getMapsTo(); 
			mapsToRepo._save(mapsToParam);
		}
		
		// then view or core, which is self in absence of mapped view
		if(r.isSelfPersistable()) {
			ModelRepository repo = getRepositoryFactory().get(r.getSelf());
			Param<?> param = p.getRootDomain().getAssociatedParam(); 
			repo._save(param);
		}
		
		return Output.instantiate(input, eCtx, Boolean.TRUE);
	}
}
