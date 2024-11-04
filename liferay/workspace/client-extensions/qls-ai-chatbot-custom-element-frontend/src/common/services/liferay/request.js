export function getServerUrl(oAuth2Client) {
	return oAuth2Client.homePageURL;
}

export async function getOAuthToken(oAuth2Client) {
	const prom = new Promise((resolve, reject) => {
		oAuth2Client
			._getOrRequestToken()
			.then(
				(token) => {
					resolve(token.access_token);
				},
				(error) => {
					showError('Error', error);

					reject(error);
				}
			)
			.catch((error) => {
				showError('Error', error);

				reject(error);
			});
	});

	return prom;
}

export async function oAuthRequest(oAuth2Client, config) {
	return request({
		...config,
		headers: {
			Authorization: `Bearer ${await getOAuthToken(oAuth2Client)}`,
			...config.headers
		},
	});
}

export const getSelectedLanguage = () => {
	return Liferay.ThemeDisplay.getLanguageId().split('_')[0];
}

export function request(config) {
	return new Promise((resolve, reject) => {
		fetch(config.url, {
			method: 'GET',
			...config,
			headers: {
				//'x-csrf-token': Liferay.authToken,
				'Accept-Language': getSelectedLanguage(),
				'Content-Type': 'application/json',
				...config.headers
			}
		})
			.then((response) => {

				if (response.ok) {
					resolve(response);
				} else {
					const errorBody = response.json();
					const error = `HTTP error! status: ${response.status} ${response.statusText}. Details: ${JSON.stringify(errorBody)}`;
					reject({ error, message: error || '' });
				}
			})
			.catch((error) => {
				reject({ error, message: error || '' });
			});
	});
}


export function showError(title, message) {
	Liferay.Util.openToast({ message, title, type: 'danger' });
}