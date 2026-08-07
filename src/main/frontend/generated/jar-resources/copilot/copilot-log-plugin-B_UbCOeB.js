import { n as e } from "./chunk-DiqZc92J.js";
import { $ as t, C as n, D as r, at as i, dt as a, et as o, n as s, ot as c, r as l, t as u, u as d, w as f } from "./icons-Daxdm7Mp.js";
import { a as p, d as m, i as h, l as g, n as _, o as v, r as y, s as b, t as x } from "./section-panel-ui-state-ByeyD6ou.js";
import { n as S, t as C } from "./copilot-eventbus-tR06exIW.js";
import { n as w, r as T } from "./copilot-ui-state-DjlBBpyf.js";
import { r as E, t as D } from "./stats-D_j8yo6z.js";
import { n as O, o as k, t as A } from "./copilot-error-handler-B7az3wX2.js";
import { n as j, t as M } from "./base-panel-Cq1wQn0M.js";
import { n as N, t as P } from "./copilot-message-box-DbAltoiu.js";
//#region frontend/copilot/copilot-time-formatter.ts
var F, I, L = e((() => {
	F = () => {
		let e = {
			hour: "numeric",
			minute: "numeric",
			second: "numeric",
			fractionalSecondDigits: 3
		}, t, n = navigator.language ?? "", r = n.indexOf("@"), i = r === -1 ? n : n.slice(0, r);
		try {
			t = new Intl.DateTimeFormat(Intl.getCanonicalLocales(i), e);
		} catch (n) {
			console.error("Failed to create date time formatter for ", i, n), t = new Intl.DateTimeFormat("en-US", e);
		}
		return t;
	}, I = F();
}));
//#endregion
//#region frontend/copilot/plugins/copilot-log/copilot-log-plugin.ts
function R(e) {
	return I.format(e);
}
var z, B, V, H, U, W, G;
//#endregion
e((() => {
	l(), b(), o(), j(), S(), T(), i(), n(), h(), s(), k(), D(), P(), x(), L(), v(), V = class {
		constructor() {
			this.showTimestamps = !1, c(this);
		}
		toggleShowTimestamps() {
			this.showTimestamps = !this.showTimestamps;
		}
	}, H = new V(), U = (z = class extends M {
		constructor(...e) {
			super(...e), this.unreadErrors = !1, this.messages = [], this.nextMessageId = 1, this.transitionDuration = 0, this.errorHandlersAdded = !1;
		}
		connectedCallback() {
			if (super.connectedCallback(), this.classList.add("contents"), this.onCommand("log", (e) => {
				this.handleLogEventData({
					type: e.data.type,
					message: e.data.message
				});
			}), this.onEventBus("log", (e) => this.handleLogEvent(e)), this.onEventBus("update-log", (e) => this.updateLog(e.detail)), this.onEventBus("notification-shown", (e) => this.handleNotification(e)), this.onEventBus("clear-log", () => this.clear()), this.reaction(() => w.sectionPanelResizing, () => {
				this.requestUpdate();
			}), this.transitionDuration = parseInt(window.getComputedStyle(this).getPropertyValue("--dev-tools-transition-duration"), 10), !this.errorHandlersAdded) {
				let e = (e) => {
					a(() => {
						_.attentionRequiredPanelTag = "copilot-log-panel";
					}), this.log(t.ERROR, e.message, !!e.internal, e.details, e.link);
				};
				A((t) => {
					e(t);
				}), O.forEach((t) => {
					e(t);
				}), O.length = 0, this.errorHandlersAdded = !0;
			}
		}
		clear() {
			this.messages = [];
		}
		handleNotification(e) {
			this.log(e.detail.type, e.detail.message, !0, e.detail.details, e.detail.link);
		}
		handleLogEvent(e) {
			this.handleLogEventData(e.detail);
		}
		handleLogEventData(e) {
			this.log(e.type, e.message, !!e.internal, e.details, e.link, r(e.expandedMessage), r(e.expandedDetails), e.id);
		}
		activate() {
			this.unreadErrors = !1, this.updateComplete.then(() => {
				let e = this.renderRoot.querySelector(".message:last-child");
				e && e.scrollIntoView();
			});
		}
		render() {
			return d`
      ${this.messages.length === 0 ? N("info", "Communication between application and backend services, errors, and all notifications will appear here.", "mb-3 mt-0 mx-3") : d`<ul class="border-dashed divide-y list-none m-0 pb-4 px-4">
            ${this.messages.map((e) => this.renderMessage(e))}
          </ul>`}
    `;
		}
		renderMessage(e) {
			let n, i, a;
			e.type === t.ERROR ? (i = u.warning, n = "Error", a = "text-ruby-11") : e.type === t.WARNING ? (i = u.warning, n = "Warning", a = "text-amber-11") : (i = u.info, n = "Info", a = "text-blue-11");
			let o = e.expanded ? "" : "truncate";
			return d`
      <li class="flex gap-2 py-2" data-id="${e.id}">
        <vaadin-icon
          aria-label="${n}"
          class="${a}"
          id="log-icon-${e.id}"
          .svg="${i}"></vaadin-icon>
        <vaadin-tooltip for="log-icon-${e.id}" text="${n}"></vaadin-tooltip>
        <span class="flex flex-col flex-grow overflow-hidden" @click=${() => this.toggleExpanded(e)}>
          <span class="${o}" ?hidden=${!H.showTimestamps}>${R(e.timestamp)}</span>
          <span class="${o}">
            ${e.expanded && e.expandedMessage ? e.expandedMessage : e.message}
          </span>
          ${e.expanded ? d`<span id="log-details-${e.id}"
                >${e.expandedDetails ?? e.details}</span
              >` : d`<span
                class="${o}"
                id="log-details-${e.id}"
                ?hidden="${!e.details && !e.link}">
                ${r(e.details)}
                ${e.link ? d`<a class="block mb-1" href="${e.link}" target="_blank">Learn more</a>` : ""}
              </span>`}
        </span>
        <vaadin-button
          aria-controls="log-details-${e.id}"
          aria-expanded="${e.expanded}"
          aria-label="${e.expanded ? "Collapse details" : "Expand details"}"
          theme="icon tertiary"
          @click=${() => this.toggleExpanded(e)}
          ?hidden=${!this.canBeExpanded(e)}>
          <vaadin-icon
            class="${e.expanded ? "rotate-90" : ""} transition"
            .svg="${u.chevronRight}"></vaadin-icon>
          <vaadin-tooltip
            slot="tooltip"
            text="${e.expanded ? "Collapse details" : "Expand details"}"></vaadin-tooltip>
        </vaadin-button>
      </li>
    `;
		}
		log(e, n, r, i, a, o, s, c) {
			let l = this.nextMessageId;
			this.nextMessageId += 1, s ||= n;
			let u = {
				id: l,
				type: e,
				message: n,
				details: i,
				link: a,
				dontShowAgain: !1,
				deleted: !1,
				expanded: !1,
				expandedMessage: o,
				expandedDetails: s,
				timestamp: /* @__PURE__ */ new Date(),
				internal: r,
				userId: c
			};
			for (this.messages.push(u); this.messages.length > B.MAX_LOG_ROWS;) this.messages.shift();
			return this.requestUpdate(), this.updateComplete.then(() => {
				let n = this.renderRoot.querySelector(".message:last-child");
				n ? (setTimeout(() => n.scrollIntoView({ behavior: "smooth" }), this.transitionDuration), this.unreadErrors = !1) : e === t.ERROR && (this.unreadErrors = !0);
			}), u;
		}
		updateLog(e) {
			let n = this.messages.find((t) => t.userId === e.id);
			n ||= this.log(t.INFORMATION, "<Log message to update was not found>", !1), Object.assign(n, e), f(n.expandedDetails) && (n.expandedDetails = r(n.expandedDetails)), this.requestUpdate();
		}
		updated() {
			let e = this.querySelector(".row:last-child");
			e && this.isTooLong(e.querySelector(".firstrowmessage")) && e.querySelector("button.expand")?.removeAttribute("hidden");
		}
		toggleExpanded(e) {
			this.canBeExpanded(e) && (e.expanded = !e.expanded, this.requestUpdate()), E("use-log", { source: "toggleExpanded" });
		}
		canBeExpanded(e) {
			if (e.expandedMessage || e.expanded) return !0;
			let t = this.querySelector(`[data\\-id="${e.id}"]`)?.querySelector(".firstrowmessage");
			return this.isTooLong(t);
		}
		isTooLong(e) {
			return e && e.offsetWidth < e.scrollWidth;
		}
	}, B = z, z.MAX_LOG_ROWS = 1e3, z), p([g()], U.prototype, "unreadErrors", void 0), p([g()], U.prototype, "messages", void 0), U = B = p([m("copilot-log-panel")], U), W = class extends y {
		createRenderRoot() {
			return this;
		}
		render() {
			return d`
      <style>
        copilot-log-panel-actions {
          display: contents;
        }
      </style>
      <vaadin-button
        aria-label="Clear log"
        @click=${() => {
				C.emit("clear-log", {});
			}}
        theme="icon tertiary">
        <vaadin-icon .svg="${u.delete}"></vaadin-icon>
        <vaadin-tooltip slot="tooltip" text="Clear log"></vaadin-tooltip>
      </vaadin-button>
      <vaadin-button
        aria-label="Toggle timestamps"
        @click=${() => {
				H.toggleShowTimestamps();
			}}
        theme="icon tertiary">
        <vaadin-icon .svg="${H.showTimestamps ? u.schedule : u.historyToggleOff}"></vaadin-icon>
        <vaadin-tooltip slot="tooltip" text="Toggle timestamps"></vaadin-tooltip>
      </vaadin-button>
    `;
		}
	}, W = p([m("copilot-log-panel-actions")], W), G = {
		header: "Log",
		tag: "copilot-log-panel",
		actionsTag: "copilot-log-panel-actions",
		individual: !0,
		toolbarOptions: {
			allowedModesWithOrder: { common: 0 },
			iconKey: "terminal"
		}
	}, window.Vaadin.copilot.plugins.push({ init(e) {
		e.addPanel(G);
	} }), _.addPanel(G);
}))();
export { W as Actions, U as CopilotLogPanel };
