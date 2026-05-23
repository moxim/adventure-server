import { q as R, a6 as M, a7 as f, G as c, x as n, D as p, a8 as S, y as b, M as L, c as D, a9 as k, m as I, p as v, n as y } from "./copilot-Bm5k-h74.js";
import { r as w } from "./state-BH_9RcTJ.js";
import { B as q } from "./base-panel-Cxtbncvf.js";
import { i as s } from "./icons-mWh87_oB.js";
const C = 'copilot-log-panel ul{list-style-type:none;margin:0;padding:0}copilot-log-panel ul li{align-items:start;display:flex;gap:var(--space-50);padding:var(--space-100) var(--space-50);position:relative}copilot-log-panel ul li:before{border-bottom:1px dashed var(--divider-primary-color);content:"";inset:auto 0 0 calc(var(--copilot-size-md) + var(--space-100));position:absolute}copilot-log-panel ul li span.icon{display:flex;flex-shrink:0;justify-content:center;width:var(--copilot-size-md)}copilot-log-panel ul li.information span.icon{color:var(--blue-color)}copilot-log-panel ul li.warning span.icon{color:var(--warning-color)}copilot-log-panel ul li.error span.icon{color:var(--error-color)}copilot-log-panel ul li .message{display:flex;flex-direction:column;flex-grow:1;overflow:hidden}copilot-log-panel ul li:not(.expanded) span{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}copilot-log-panel ul li button svg{transition:transform .15s cubic-bezier(.2,0,0,1)}copilot-log-panel ul li button[aria-expanded=true] svg{transform:rotate(90deg)}copilot-log-panel ul li code{margin-top:var(--space-50)}copilot-log-panel ul li.expanded .secondary{margin-top:var(--space-100)}copilot-log-panel .secondary a{display:block;margin-bottom:var(--space-50)}', P = () => {
  const e = { hour: "numeric", minute: "numeric", second: "numeric", fractionalSecondDigits: 3 };
  let t;
  const a = navigator.language ?? "", i = a.indexOf("@"), o = i === -1 ? a : a.slice(0, i);
  try {
    t = new Intl.DateTimeFormat(Intl.getCanonicalLocales(o), e);
  } catch (l) {
    console.error("Failed to create date time formatter for ", o, l), t = new Intl.DateTimeFormat("en-US", e);
  }
  return t;
}, A = P();
var _ = Object.defineProperty, B = Object.getOwnPropertyDescriptor, u = (e, t, a, i) => {
  for (var o = i > 1 ? void 0 : i ? B(t, a) : t, l = e.length - 1, r; l >= 0; l--)
    (r = e[l]) && (o = (i ? r(t, a, o) : r(o)) || o);
  return i && o && _(t, a, o), o;
};
class F {
  constructor() {
    this.showTimestamps = !1, I(this);
  }
  toggleShowTimestamps() {
    this.showTimestamps = !this.showTimestamps;
  }
}
const h = new F();
let d = class extends q {
  constructor() {
    super(...arguments), this.unreadErrors = !1, this.messages = [], this.nextMessageId = 1, this.transitionDuration = 0, this.errorHandlersAdded = !1;
  }
  connectedCallback() {
    if (super.connectedCallback(), this.onCommand("log", (e) => {
      this.handleLogEventData({ type: e.data.type, message: e.data.message });
    }), this.onEventBus("log", (e) => this.handleLogEvent(e)), this.onEventBus("update-log", (e) => this.updateLog(e.detail)), this.onEventBus("notification-shown", (e) => this.handleNotification(e)), this.onEventBus("clear-log", () => this.clear()), this.reaction(
      () => R.sectionPanelResizing,
      () => {
        this.requestUpdate();
      }
    ), this.transitionDuration = parseInt(
      window.getComputedStyle(this).getPropertyValue("--dev-tools-transition-duration"),
      10
    ), !this.errorHandlersAdded) {
      const e = (t) => {
        k(() => {
          v.attentionRequiredPanelTag = "copilot-log-panel";
        }), this.log(p.ERROR, t.message, !!t.internal, t.details, t.link);
      };
      M((t) => {
        e(t);
      }), f.forEach((t) => {
        e(t);
      }), f.length = 0, this.errorHandlersAdded = !0;
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
    this.log(
      e.type,
      e.message,
      !!e.internal,
      e.details,
      e.link,
      c(e.expandedMessage),
      c(e.expandedDetails),
      e.id
    );
  }
  activate() {
    this.unreadErrors = !1, this.updateComplete.then(() => {
      const e = this.renderRoot.querySelector(".message:last-child");
      e && e.scrollIntoView();
    });
  }
  render() {
    return n`
      <style>
        ${C}
      </style>
      ${this.messages.length === 0 ? n`<div
            class="bg-blue-3 dark:bg-blue-5 gap-2 flex mb-3 mt-0 mx-3 pe-3 ps-2 py-2 rounded-md text-blue-11 dark:text-blue-12 text-sm">
            <vaadin-icon .svg="${s.info}"></vaadin-icon>
            <span
              >Communication between application and backend services, errors, and all notifications will appear
              here.</span
            >
          </div>` : n`<ul>
            ${this.messages.map((e) => this.renderMessage(e))}
          </ul>`}
    `;
  }
  renderMessage(e) {
    let t, a;
    return e.type === p.ERROR ? (a = s.alertTriangle, t = "Error") : e.type === p.WARNING ? (a = s.warning, t = "Warning") : (a = s.info, t = "Info"), n`
      <li
        class="${e.type} ${e.expanded ? "expanded" : ""} ${e.details || e.link ? "has-details" : ""}"
        data-id="${e.id}">
        <span aria-label="${t}" class="icon" title="${t}">${a}</span>
        <span class="message" @click=${() => this.toggleExpanded(e)}>
          <span class="timestamp" ?hidden=${!h.showTimestamps}>${W(e.timestamp)}</span>
          <span class="primary">
            ${e.expanded && e.expandedMessage ? e.expandedMessage : e.message}
          </span>
          ${e.expanded ? n` <span class="secondary"> ${e.expandedDetails ?? e.details} </span>` : n` <span class="secondary" ?hidden="${!e.details && !e.link}">
                ${c(e.details)}
                ${e.link ? n` <a href="${e.link}" target="_blank">Learn more</a>` : ""}
              </span>`}
        </span>
        <!-- TODO: a11y, button needs aria-controls with unique ids -->
        <button
          aria-controls="content"
          aria-expanded="${e.expanded}"
          aria-label="Expand details"
          class="icon"
          @click=${() => this.toggleExpanded(e)}
          ?hidden=${!this.canBeExpanded(e)}>
          <span>${s.chevronRight}</span>
        </button>
      </li>
    `;
  }
  log(e, t, a, i, o, l, r, E) {
    const T = this.nextMessageId;
    this.nextMessageId += 1, r || (r = t);
    const g = {
      id: T,
      type: e,
      message: t,
      details: i,
      link: o,
      dontShowAgain: !1,
      deleted: !1,
      expanded: !1,
      expandedMessage: l,
      expandedDetails: r,
      timestamp: /* @__PURE__ */ new Date(),
      internal: a,
      userId: E
    };
    for (this.messages.push(g); this.messages.length > d.MAX_LOG_ROWS; )
      this.messages.shift();
    return this.requestUpdate(), this.updateComplete.then(() => {
      const m = this.renderRoot.querySelector(".message:last-child");
      m ? (setTimeout(() => m.scrollIntoView({ behavior: "smooth" }), this.transitionDuration), this.unreadErrors = !1) : e === p.ERROR && (this.unreadErrors = !0);
    }), g;
  }
  updateLog(e) {
    let t = this.messages.find((a) => a.userId === e.id);
    t || (t = this.log(p.INFORMATION, "<Log message to update was not found>", !1)), Object.assign(t, e), S(t.expandedDetails) && (t.expandedDetails = c(t.expandedDetails)), this.requestUpdate();
  }
  updated() {
    const e = this.querySelector(".row:last-child");
    e && this.isTooLong(e.querySelector(".firstrowmessage")) && e.querySelector("button.expand")?.removeAttribute("hidden");
  }
  toggleExpanded(e) {
    this.canBeExpanded(e) && (e.expanded = !e.expanded, this.requestUpdate()), b("use-log", { source: "toggleExpanded" });
  }
  canBeExpanded(e) {
    if (e.expandedMessage || e.expanded)
      return !0;
    const t = this.querySelector(`[data\\-id="${e.id}"]`)?.querySelector(
      ".firstrowmessage"
    );
    return this.isTooLong(t);
  }
  isTooLong(e) {
    return e && e.offsetWidth < e.scrollWidth;
  }
};
d.MAX_LOG_ROWS = 1e3;
u([
  w()
], d.prototype, "unreadErrors", 2);
u([
  w()
], d.prototype, "messages", 2);
d = u([
  y("copilot-log-panel")
], d);
let x = class extends L {
  createRenderRoot() {
    return this;
  }
  render() {
    return n`
      <style>
        copilot-log-panel-actions {
          display: contents;
        }
      </style>
      <vaadin-button
        aria-label="Clear log"
        @click=${() => {
      D.emit("clear-log", {});
    }}
        theme="icon tertiary">
        <vaadin-icon .svg="${s.delete}"></vaadin-icon>
        <vaadin-tooltip slot="tooltip" text="Clear log"></vaadin-tooltip>
      </vaadin-button>
      <vaadin-button
        aria-label="Toggle timestamps"
        @click=${() => {
      h.toggleShowTimestamps();
    }}
        theme="icon tertiary">
        <vaadin-icon .svg="${h.showTimestamps ? s.schedule : s.historyToggleOff}"></vaadin-icon>
        <vaadin-tooltip slot="tooltip" text="Toggle timestamps"></vaadin-tooltip>
      </vaadin-button>
    `;
  }
};
x = u([
  y("copilot-log-panel-actions")
], x);
const $ = {
  header: "Log",
  tag: "copilot-log-panel",
  actionsTag: "copilot-log-panel-actions",
  individual: !0,
  toolbarOptions: {
    allowedModesWithOrder: {
      common: 0
    },
    iconKey: "terminal"
  }
}, U = {
  init(e) {
    e.addPanel($);
  }
};
window.Vaadin.copilot.plugins.push(U);
v.addPanel($);
function W(e) {
  return A.format(e);
}
export {
  x as Actions,
  d as CopilotLogPanel
};
