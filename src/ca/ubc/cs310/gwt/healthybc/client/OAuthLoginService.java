/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ca.ubc.cs310.gwt.healthybc.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("OAuthLoginService")
public interface OAuthLoginService extends RemoteService
{
    public String     getAuthorizationUrl(Credential credential) throws LoginAuthException;
    public SocialUser verifySocialUser(Credential credential) throws LoginAuthException;
    public SocialUser fetchMe(String sessionId) throws LoginAuthException;
    public String     getAccessToken(String sessionId) throws LoginAuthException;
    public void       logout(String sessionId) throws LoginAuthException;
    /**
     * Utility class for simplifying access to the instance of async service.
     */
    public static class Util
    {
        private static OAuthLoginServiceAsync instance;

        public static OAuthLoginServiceAsync getInstance()
        {
            if (instance == null)
            {
                instance=GWT.create(OAuthLoginService.class);
            }
            return instance;
        }
    }
}
