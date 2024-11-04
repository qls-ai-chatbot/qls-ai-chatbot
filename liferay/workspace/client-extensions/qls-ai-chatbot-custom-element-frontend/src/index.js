/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React from 'react';
import {createRoot} from 'react-dom/client';

import Assistant from './common/components/Assistant.js';

import './common/styles/index.scss';

const App = ({route}) => {

	return (
		<div>
			<Assistant/>
		</div>
	);
};

class WebComponent extends HTMLElement {
	connectedCallback() {

		this.root = createRoot(this);

		this.root.render(<App route={this.getAttribute('route')} />, this);
	}

	disconnectedCallback() {

		//
		// Unmount React tree to prevent memory leaks.
		//
		// See React documentation at
		//
		//     https://react.dev/reference/react-dom/client/createRoot#root-unmount
		//
		// for more information.
		//

		this.root.unmount();
		delete this.root;
	}
}

const ELEMENT_ID = 'qls-ai-chatbot';

if (!customElements.get(ELEMENT_ID)) {
	customElements.define(ELEMENT_ID, WebComponent);
}
