import { n as e } from "./chunk-DiqZc92J.js";
import { n as t, r as n, t as r, u as i } from "./icons-Daxdm7Mp.js";
//#region frontend/copilot/shared/copilot-message-box.ts
function a(e, t, n, r) {
	let { bg: a, iconClass: s, textClass: c, icon: l } = o[e], u = r?.icon ?? l, d = r?.iconExtraClass ? `${s} ${r.iconExtraClass}` : s;
	return i`
    <div class="${`${a} flex gap-2 pe-3 ps-2 py-2 rounded-md text-sm${n ? ` ${n}` : ""}`}">
      <vaadin-icon class="${d}" .svg="${u}"></vaadin-icon>
      ${typeof t == "string" ? i`<span class="message-box-content${c ? ` ${c}` : ""}">${t}</span>` : t}
    </div>
  `;
}
var o, s = e((() => {
	n(), t(), o = {
		info: {
			bg: "bg-blue-3 dark:bg-blue-5",
			iconClass: "text-blue-11",
			textClass: "text-blue-12",
			icon: r.info
		},
		warning: {
			bg: "bg-amber-3 dark:bg-amber-5",
			iconClass: "text-amber-11",
			textClass: "text-amber-12",
			icon: r.warning
		},
		error: {
			bg: "bg-ruby-3 dark:bg-ruby-5",
			iconClass: "text-ruby-11",
			textClass: "text-ruby-12",
			icon: r.error
		},
		success: {
			bg: "bg-teal-3 dark:bg-teal-5",
			iconClass: "text-teal-11",
			textClass: "text-teal-12",
			icon: r.check
		},
		loading: {
			bg: "bg-gray-3 dark:bg-gray-6",
			iconClass: "animate-spin",
			textClass: "",
			icon: r.progressActivity
		}
	};
}));
//#endregion
export { a as n, s as t };
