//#region node_modules/ol/size.js
/**
* Determines if a size has a positive area.
* @param {Size} size The size to test.
* @return {boolean} The size has a positive area.
*/
function hasArea(size) {
	return size[0] > 0 && size[1] > 0;
}
/**
* Returns a size scaled by a ratio. The result will be an array of integers.
* @param {Size} size Size.
* @param {number} ratio Ratio.
* @param {Size} [dest] Optional reusable size array.
* @return {Size} The scaled size.
*/
function scale(size, ratio, dest) {
	if (dest === void 0) dest = [0, 0];
	dest[0] = size[0] * ratio + .5 | 0;
	dest[1] = size[1] * ratio + .5 | 0;
	return dest;
}
/**
* Returns an `Size` array for the passed in number (meaning: square) or
* `Size` array.
* (meaning: non-square),
* @param {number|Size} size Width and height.
* @param {Size} [dest] Optional reusable size array.
* @return {Size} Size.
* @api
*/
function toSize(size, dest) {
	if (Array.isArray(size)) return size;
	if (dest === void 0) dest = [size, size];
	else {
		dest[0] = size;
		dest[1] = size;
	}
	return dest;
}
//#endregion
export { scale as n, toSize as r, hasArea as t };

//# sourceMappingURL=size-Bir6Ad0C.js.map