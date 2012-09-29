/*   Copyright 2012 Christopher Perry Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

package com.cperryinc.billing.example;

import com.cperryinc.robobilling.BillingMode;
import com.cperryinc.robobilling.RoboBillingApplication;
import net.robotmedia.billing.utils.IConfiguration;

public class Application extends RoboBillingApplication {
    private BillingMode billingMode;
    private IConfiguration configuration;

    // A randomly generated salt. You should generate your own for your application.
    private static final byte[] salt =
            new byte[]{41, -90, -116, -41, 66, -53, 122, -110, -127, -96, -88, 77, 127, 115, 1, 73, 57, 110, 48, -116};

    @Override
    public IConfiguration getConfiguration() {
        if (configuration == null) {
            configuration = new IConfiguration() {
                /**
                 * For Amazon we use the currently logged in user as the salt, to
                 * differentiate between different users when we save purchased items.
                 *
                 * @return The salt to use when obfuscating purchases in the database and
                 * making calls to the Google server
                 */
                @Override
                public byte[] getObfuscationSalt() {
                    return billingMode == BillingMode.GOOGLE ? salt : getUser().getUserId().getBytes();
                }

                /**
                 * Important: To keep your public key safe from malicious users and hackers, do not embed your public key as
                 * an entire literal string. Instead, construct the string at runtime from pieces or use bit manipulation
                 * (for example, XOR with some other string) to hide the actual key. The key itself is not secret information,
                 * but you do not want to make it easy for a hacker or malicious user to replace the public key with another key.
                 */
                @Override
                public String getPublicKey() {
                    return "your key here";
                }
            };
        }
        return configuration;
    }

}
