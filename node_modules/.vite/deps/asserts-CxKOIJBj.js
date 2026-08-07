//#region node_modules/ol/asserts.js
/**
* @module ol/asserts
*/
/**
* @param {*} assertion Assertion we expected to be truthy.
* @param {string} errorMessage Error message.
*/
function assert(assertion, errorMessage) {
	if (!assertion) throw new Error(errorMessage);
}
//#endregion
export { assert as t };

//# sourceMappingURL=asserts-CxKOIJBj.js.map