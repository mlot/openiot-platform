/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.openiot.server.asset.scim;

import com.openiot.rest.model.asset.PersonAsset;

/**
 * Person asset loaded from the WSO2 SCIM interface.
 * 
 * @author dadams
 */
public class Wso2ScimAsset extends PersonAsset {

	/*
	 * (non-Javadoc)
	 * 
	 * @see PersonAsset#getUserNameProperty()
	 */
	public String getUserNameProperty() {
		return IWso2ScimFields.PROP_USERNAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see PersonAsset#getNameProperty()
	 */
	public String getNameProperty() {
		return IWso2ScimFields.PROP_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see PersonAsset#getEmailAddressProperty()
	 */
	public String getEmailAddressProperty() {
		return IWso2ScimFields.PROP_EMAIL_ADDRESS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Asset#getIdProperty()
	 */
	public String getIdProperty() {
		return IWso2ScimFields.PROP_ASSET_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IPersonAsset#getPhotoUrlProperty()
	 */
	public String getPhotoUrlProperty() {
		return IWso2ScimFields.PROP_PROFILE_URL;
	}
}