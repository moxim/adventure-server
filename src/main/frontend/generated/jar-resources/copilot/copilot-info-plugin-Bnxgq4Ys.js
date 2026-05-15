import { q as r, x as i, E as l, p as m, aa as g, ab as b, M as x, c as y, n as v } from "./copilot-Bm5k-h74.js";
import { r as h } from "./state-BH_9RcTJ.js";
import { B as w } from "./base-panel-Cxtbncvf.js";
import { i as t } from "./icons-mWh87_oB.js";
import { c as $, a as C } from "./repeat-xjtCsDSL.js";
var I = Object.defineProperty, V = Object.getOwnPropertyDescriptor, u = (e, a, n, o) => {
  for (var s = o > 1 ? void 0 : o ? V(a, n) : a, c = e.length - 1, d; c >= 0; c--)
    (d = e[c]) && (s = (o ? d(a, n, s) : d(s)) || s);
  return o && s && I(a, n, s), s;
};
let p = class extends w {
  constructor() {
    super(...arguments), this.sortedEntries = [];
  }
  connectedCallback() {
    super.connectedCallback(), this.classList.add("contents"), this.reaction(
      () => r.projectInfoEntries,
      () => {
        if (!r.projectInfoEntries)
          return;
        let e = [
          ...r.projectInfoEntries,
          { name: "Development Workflow", value: "" }
        ];
        e = e.filter((a) => a.name !== "Java Hotswap"), this.sortedEntries = e.sort((a, n) => a.name.localeCompare(n.name));
      },
      { fireImmediately: !0 }
    );
  }
  render() {
    return i` <div class="flex flex-col py-2 px-4">
      <dl class="border-dashed divide-y m-0">
        ${$(
      this.sortedEntries.filter((e) => e.name !== "Java Hotswap"),
      (e) => e.name,
      (e) => this.renderRow(e)
    )}
      </dl>
    </div>`;
  }
  renderRow(e) {
    if (e.name === "Development Workflow")
      return this.renderDevelopmentWorkflowButton();
    const a = e.name === "IDE Plugin" && e.value === !0 && r.idePluginState?.ide ? r.idePluginState.ide : e.value, n = this.getIcon(e.name, a), o = this.getIconColor(e.name), s = this.getTextColor(e);
    return i`
      <div class="flex gap-2 py-2">
        <dt class="flex gap-2">
          ${n ? i`<vaadin-icon class="${o}" .svg="${n}"></vaadin-icon>` : l} ${e.name}
        </dt>
        <dd class="flex gap-2 m-0 ${s}">${this.renderRowValue(e)}</dd>
      </div>
    `;
  }
  renderRowValue(e) {
    return e.name === "Vaadin Employee" && e.value === !0 ? i`
        <vaadin-icon id="vaadin-employee" class="text-teal-11" .svg="${t.check}"></vaadin-icon>
        <vaadin-tooltip for="vaadin-employee" text="Yes"></vaadin-tooltip>
      ` : i` ${!e.booleanInfo && typeof e.value == "string" ? e.value : l}
    ${e.booleanInfo && typeof e.value == "boolean" ? S(e.value, e.booleanInfo.ariaLabel) : l}
    ${e.booleanInfo?.text ? e.booleanInfo.text : l}
    ${e.name === "Vaadin" ? this.renderVaadinRowMore() : l}`;
  }
  renderVaadinRowMore() {
    const e = r.newVaadinVersionState?.versions !== void 0 && r.newVaadinVersionState.versions.length > 0;
    return i`
      ${r.projectVersionReleaseNoteInfo && r.projectVersionReleaseNoteInfo.url ? i`<a
            class="flex gap-0.5 items-center"
            href="${r.projectVersionReleaseNoteInfo.url}"
            id="release-notes-link"
            target="_blank"
            >Release notes <vaadin-icon class="icon-sm" .svg="${t.arrowOutward}"></vaadin-icon
          ></a>` : l}
      <vaadin-button
        aria-label="Edit Vaadin version"
        class="-my-1.5 relative"
        @click="${(a) => {
      a.stopPropagation(), m.openPanel("copilot-vaadin-versions");
    }}"
        id="new-vaadin-version-btn"
        theme="icon tertiary">
        <vaadin-icon .svg="${t.editSquare}"></vaadin-icon>
        <vaadin-tooltip slot="tooltip" text="Edit Vaadin version"></vaadin-tooltip>
        ${e ? i`<span aria-hidden="true" class="absolute bg-amber-11 end-0.5 rounded-full size-1 top-0.5"></span>` : ""}
      </vaadin-button>
    `;
  }
  renderDevelopmentWorkflowButton() {
    const e = g();
    let a = "", n = t.doneAll, o = "";
    return e.status === "success" ? (a = "text-teal-11", o = "IDE Plugin & Java Hotswap") : e.status === "warning" ? (a = "text-amber-11", n = t.arrowUploadReady, o = "Improve") : e.status === "error" && (a = "text-ruby-11", n = t.handyman, o = "Fix"), i`
      <div class="flex gap-2 py-2">
        <dt class="flex gap-2">
          <vaadin-icon class="text-amber-11" .svg="${t.bolt}"></vaadin-icon>
          Development Workflow
        </dt>
        <dd class="m-0">
          <vaadin-button
            class="-my-1.5 ${a}"
            id="development-workflow-status-detail"
            theme="tertiary"
            @click=${() => {
      b();
    }}>
            <vaadin-icon slot="prefix" .svg="${n}"></vaadin-icon>
            ${o}
          </vaadin-button>
        </dd>
      </div>
    `;
  }
  getIconColor(e) {
    return e.includes("Vaadin") || e === "Copilot" ? "text-vaadin-blue" : "";
  }
  getTextColor(e) {
    if (typeof e.value == "string") {
      if (e.value.startsWith("Enabled"))
        return "text-teal-11";
      if (e.value.startsWith("Disabled"))
        return "text-ruby-11";
    }
    return "text-secondary";
  }
  getIcon(e, a) {
    switch (e) {
      case "Browser": {
        const n = typeof a == "string" ? a.toLowerCase() : "";
        return n.includes("chrome") && !n.includes("edg") ? t.chrome : n.includes("firefox") ? t.firefox : n.includes("safari") && !n.includes("chrome") ? t.safari : n.includes("edg") ? t.edge : t.webAsset;
      }
      case "Copilot":
        return t.vaadin;
      case "Flow":
        return t.flow;
      case "Frontend Hotswap":
        return t.swapHoriz;
      case "Java":
        return t.java;
      case "OS": {
        const n = typeof a == "string" ? a.toLowerCase() : "";
        return n.includes("mac") ? t.apple : n.includes("win") ? t.windows : t.computer;
      }
      case "Spring":
        return t.spring;
      case "Spring Boot":
        return t.springBoot;
      case "Spring Data JPA":
        return t.springData;
      case "Spring Security":
        return t.springSecurity;
      case "Vaadin":
      case "Vaadin Employee":
        return t.vaadin;
      case "Java Hotswap":
        return t.swapHoriz;
      case "IDE Plugin":
        return typeof a != "string" ? t.developerModeTv : a.toLowerCase() === "intellij" ? t.intelliJ : a.toLowerCase() === "vscode" ? t.vsCode : a.toLowerCase() === "eclipse" ? t.eclipse : t.developerModeTv;
      default:
        return null;
    }
  }
};
u([
  h()
], p.prototype, "sortedEntries", 2);
p = u([
  v("copilot-info-panel")
], p);
let f = class extends x {
  createRenderRoot() {
    return this;
  }
  connectedCallback() {
    super.connectedCallback(), this.style.display = "flex";
  }
  render() {
    return i` <vaadin-button
      aria-label="Copy to clipboard"
      @click=${() => {
      y.emit("system-info-with-callback", {
        callback: C,
        notify: !0
      });
    }}
      theme="icon tertiary">
      <vaadin-icon .svg="${t.fileCopy}"></vaadin-icon>
      <vaadin-tooltip slot="tooltip" text="Copy to clipboard"></vaadin-tooltip>
    </vaadin-button>`;
  }
};
f = u([
  v("copilot-info-actions")
], f);
const E = {
  header: "Info",
  tag: "copilot-info-panel",
  actionsTag: "copilot-info-actions",
  eager: !0,
  // Render even when collapsed as error handling depends on this
  toolbarOptions: {
    iconKey: "info",
    allowedModesWithOrder: {
      common: 0
    }
  }
}, P = {
  init(e) {
    e.addPanel(E);
  }
};
window.Vaadin.copilot.plugins.push(P);
function S(e, a) {
  let n;
  return e === !0 ? n = "text-teal-11" : e === "partial" ? n = "text-amber-11" : n = "text-ruby-11", i`<span class="${n}">${a}</span>`;
}
export {
  f as Actions,
  p as CopilotInfoPanel
};
