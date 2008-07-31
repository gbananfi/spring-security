/* Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.ldap;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.util.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * LDAP Utility methods.
 *
 * @author Luke Taylor
 * @version $Id$
 */
public final class LdapUtils {
    //~ Static fields/initializers =====================================================================================

    private static final Log logger = LogFactory.getLog(LdapUtils.class);

    //~ Constructors ===================================================================================================

    private LdapUtils() {
    }

    //~ Methods ========================================================================================================

    public static void closeContext(Context ctx) {
        if(ctx instanceof DirContextAdapter) {
            return;
        }

        try {
            if (ctx != null) {
                ctx.close();
            }
        } catch (NamingException e) {
            logger.error("Failed to close context.", e);
        }
    }

    /**
     * Obtains the part of a DN relative to a supplied base context.
     * <p>If the DN is "cn=bob,ou=people,dc=springframework,dc=org" and the base context name is
     * "ou=people,dc=springframework,dc=org" it would return "cn=bob".
     * </p>
     *
     * @param fullDn the DN
     * @param baseCtx the context to work out the name relative to.
     *
     * @return the
     *
     * @throws NamingException any exceptions thrown by the context are propagated.
     */
    public static String getRelativeName(String fullDn, Context baseCtx) throws NamingException {

        String baseDn = baseCtx.getNameInNamespace();

        if (baseDn.length() == 0) {
            return fullDn;
        }

        DistinguishedName base = new DistinguishedName(baseDn);
        DistinguishedName full = new DistinguishedName(fullDn);

        if(base.equals(full)) {
            return "";
        }

        Assert.isTrue(full.startsWith(base), "Full DN does not start with base DN");

        full.removeFirst(base);

        return full.toString();
    }

    /**
     * Gets the full dn of a name by prepending the name of the context it is relative to.
     * If the name already contains the base name, it is returned unaltered.
     */
    public static DistinguishedName getFullDn(DistinguishedName dn, Context baseCtx)
            throws NamingException {
        DistinguishedName baseDn = new DistinguishedName(baseCtx.getNameInNamespace());

        if(dn.contains(baseDn)) {
            return dn;
        }

        baseDn.append(dn);

        return baseDn;
    }

    public static byte[] getUtf8Bytes(String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Should be impossible since UTF-8 is required by all implementations
            throw new IllegalStateException("Failed to convert string to UTF-8 bytes. Shouldn't be possible");
        }
    }

    public static String getUtf8BytesAsString(byte[] utf8) {
        try {
            return new String(utf8, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Should be impossible since UTF-8 is required by all implementations
            throw new IllegalStateException("Failed to convert string to UTF-8 bytes. Shouldn't be possible");
        }
    }

    public static String convertPasswordToString(Object passObj) {
        Assert.notNull(passObj, "Password object to convert must not be null");

        if(passObj instanceof byte[]) {
            return getUtf8BytesAsString((byte[])passObj);
        } else if (passObj instanceof String) {
            return (String)passObj;
        } else {
            throw new IllegalArgumentException("Password object was not a String or byte array.");
        }
    }

    /**
     * Works out the root DN for an LDAP URL.<p>For example, the URL
     * <tt>ldap://monkeymachine:11389/dc=springframework,dc=org</tt> has the root DN "dc=springframework,dc=org".</p>
     *
     * @param url the LDAP URL
     *
     * @return the root DN
     */
    public static String parseRootDnFromUrl(String url) {
        Assert.hasLength(url);

        String urlRootDn = "";

        if (url.startsWith("ldap:") || url.startsWith("ldaps:")) {
            URI uri = parseLdapUrl(url);
            urlRootDn = uri.getRawPath();
        } else {
            // Assume it's an embedded server
            urlRootDn = url;
        }

        if (urlRootDn.startsWith("/")) {
            urlRootDn = urlRootDn.substring(1);
        }

        return urlRootDn;
    }

    /**
     * Parses the supplied LDAP URL.
     * @param url the URL (e.g. <tt>ldap://monkeymachine:11389/dc=springframework,dc=org</tt>).
     * @return the URI object created from the URL
     * @throws IllegalArgumentException if the URL is null, empty or the URI syntax is invalid.
     */

    private static URI parseLdapUrl(String url) {
        Assert.hasLength(url);

        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            IllegalArgumentException iae = new IllegalArgumentException("Unable to parse url: " + url);
            iae.initCause(e);
            throw iae;
        }
    }
}
