import { n as m, M as h, E as c, N as x, x as o, q as i, $ as v, Q as y, aw as k, y as C, p as l, am as I, ai as d } from "./copilot-Bm5k-h74.js";
import { i as t } from "./icons-mWh87_oB.js";
var $ = Object.getOwnPropertyDescriptor, w = (e, n, a, r) => {
  for (var s = r > 1 ? void 0 : r ? $(n, a) : n, p = e.length - 1, u; p >= 0; p--)
    (u = e[p]) && (s = u(s) || s);
  return s;
};
const S = "bg-[linear-gradient(to_right,var(--amber-3),var(--amber-5),var(--amber-3),var(--amber-6))] dark:bg-[linear-gradient(to_right,var(--amber-5),var(--amber-7),var(--amber-5),var(--amber-8))]", P = "bg-[linear-gradient(to_right,var(--blue-3),var(--blue-5),var(--blue-3),var(--blue-6))] dark:bg-[linear-gradient(to_right,var(--blue-4),var(--blue-6),var(--blue-4),var(--blue-7))]", E = "bg-[linear-gradient(to_right,var(--ruby-3),var(--ruby-5),var(--ruby-3),var(--ruby-6))] dark:bg-[linear-gradient(to_right,var(--ruby-4),var(--ruby-6),var(--ruby-4),var(--ruby-7))]", A = "bg-[linear-gradient(to_right,var(--teal-3),var(--teal-5),var(--teal-3),var(--teal-6))] dark:bg-[linear-gradient(to_right,var(--teal-4),var(--teal-6),var(--teal-4),var(--teal-7))]";
let g = class extends h {
  createRenderRoot() {
    return this;
  }
  connectedCallback() {
    super.connectedCallback(), this.classList.add("flex", "flex-col");
  }
  render() {
    return o`
      <header class="flex items-center pe-2 ps-4 py-2">
        <h2 class="font-bold gap-1 me-auto my-0 text-xs uppercase">Vaadin Copilot</h2>
        <vaadin-button
          aria-label="Close"
          theme="icon tertiary"
          @click=${() => {
      this.closePopover();
    }}>
          <vaadin-icon .svg="${t.close}"></vaadin-icon>
          <vaadin-tooltip slot="tooltip" text="Close"></vaadin-tooltip>
        </vaadin-button>
      </header>
      <div class="flex flex-col gap-4 pb-4 px-4">
        ${this.renderUserButton()} ${this.renderDevelopmentWorkflow()} ${this.renderWelcomeToVersion()}
        <div class="bg-gray-3 dark:bg-gray-6 flex flex-col rounded-md">
          <vaadin-button
            @click="${this.handleAppInfoClick}"
            class="border-0 h-auto justify-start py-2"
            theme="tertiary">
            <vaadin-icon slot="prefix" .svg="${t.info}"></vaadin-icon>
            App Info
          </vaadin-button>
          <vaadin-button @click="${this.handleAppLogClick}" class="border-0 h-auto justify-start py-2" theme="tertiary">
            <vaadin-icon slot="prefix" .svg="${t.terminal}"></vaadin-icon>
            App Log
          </vaadin-button>
          <vaadin-button
            @click="${this.handleFeaturesClick}"
            class="border-0 h-auto justify-start py-2"
            theme="tertiary">
            <vaadin-icon slot="prefix" .svg="${t.listAlt}"></vaadin-icon>
            Features
          </vaadin-button>
          ${x.springSecurityEnabled ? o`
                <vaadin-button
                  @click="${this.handleImpersonateAppUserClick}"
                  class="border-0 h-auto justify-start py-2"
                  theme="tertiary">
                  <vaadin-icon slot="prefix" .svg="${t.accountCircle}"></vaadin-icon>
                  Impersonate App User
                </vaadin-button>
              ` : c}
        </div>
        <div class="bg-gray-3 dark:bg-gray-6 flex flex-col rounded-md">
          <vaadin-button
            @click="${this.handleHelpAndSupportClick}"
            class="border-0 h-auto justify-start py-2"
            theme="tertiary">
            <vaadin-icon slot="prefix" .svg="${t.help}"></vaadin-icon>
            Help & Support
          </vaadin-button>
          <vaadin-button
            @click="${this.handleSettingsClick}"
            class="border-0 h-auto justify-start py-2"
            theme="tertiary">
            <vaadin-icon slot="prefix" .svg="${t.settings}"></vaadin-icon>
            Settings
          </vaadin-button>
        </div>
      </div>
    `;
  }
  renderUserButton() {
    const e = i.userInfo?.validLicense, n = e ? S : P, a = e ? "text-amber-12 dark:text-amber-11" : "text-blue-12 dark:text-blue-11", r = this.getUserName() !== "Log in";
    return o`
      <vaadin-button
        @click=${this.handleUserLoginClick}
        class="animate-gradient ${n} border-0 h-auto justify-start py-2 text-start ${r ? "gap-3 px-3" : "items-start"}">
        ${r ? this.renderUserImage() : o`<vaadin-icon
              class="text-blue-12 dark:text-blue-11"
              slot="prefix"
              .svg="${t.login}"></vaadin-icon>`}
        <span class="flex flex-col">
          <span>${this.getUserName()}</span>
          <span class="${a} text-xs">${this.getLicenseType()}</span>
        </span>
      </vaadin-button>
    `;
  }
  renderWelcomeToVersion() {
    const e = i.projectVersionReleaseNoteInfo;
    return e === null ? c : v.getMostRecentReleaseNoteDismissed() ? c : e.mostRecentVersion ? e.url ? o`
      <vaadin-button
        id="release-note-btn"
        data-test-id="release-note-btn"
        class="border-0 h-auto justify-start py-2"
        @click="${(n) => {
      window.open(e.url, "_blank");
    }}">
        <vaadin-icon class="text-teal-11" slot="prefix" .svg="${t.info}"></vaadin-icon>
        <span class="flex flex-col items-start">
          <span>Welcome to Vaadin ${e.vaadinVersion}</span>
          <span class="text-teal-11">Click for release notes</span>
        </span>
        <vaadin-icon
          id="dismiss-release-note-item"
          class="text-teal-11"
          slot="suffix"
          @click="${(n) => {
      n.stopPropagation(), v.setMostRecentReleaseNoteDismissed(!0);
    }}"
          .svg="${t.close}"></vaadin-icon>
      </vaadin-button>
      <vaadin-tooltip for="dismiss-release-note-item" text="Dismiss"></vaadin-tooltip>
    ` : c : c;
  }
  renderUserImage() {
    return i.userInfo?.portraitUrl ? o`<img
        alt="${this.getUserName()}"
        class="rounded-full size-8 object-cover"
        slot="prefix"
        src="https://vaadin.com${i.userInfo.portraitUrl}" />` : c;
  }
  renderDevelopmentWorkflow() {
    const e = y(), n = k(), a = this.getDevelopmentWorkflowConfig(e, n), r = a?.bgClass ?? "", s = a?.colorClass ?? "", p = this.resolveIcon(a), u = a?.rotateIcon ? `rotate-180 ${s}` : s, b = this.resolveTitle(a), f = a?.displayMessage ?? "";
    return o`
      <vaadin-button
        data-test-id="development-workflow-btn"
        @click="${this.handleDevelopmentWorkflowClick}"
        class="animation-delay-4000 animate-gradient ${r} border-0 h-auto justify-start py-2 text-start">
        <vaadin-icon class="${u}" slot="prefix" .svg="${p}"></vaadin-icon>
        <span class="flex flex-col">
          <span>${b}</span>
          <span class="text-xs ${s}">${f}</span>
        </span>
      </vaadin-button>
    `;
  }
  getDevelopmentWorkflowConfig(e, n) {
    const a = {
      bgClass: A,
      colorClass: "text-teal-11"
    };
    if (e === "warning" && n === "warning")
      return {
        ...a,
        icon: t.wbIncandescent,
        rotateIcon: !0,
        title: "IDE plugin & Hotswap recommended",
        combinedTitle: !0,
        displayMessage: "Enable both for optimal development workflow"
      };
    if (e === "warning")
      return {
        ...a,
        icon: t.wbIncandescent,
        rotateIcon: !0,
        title: "Hotswap recommended",
        displayMessage: "Applies changes without restarting"
      };
    if (n === "warning")
      return {
        ...a,
        icon: t.code,
        getIcon: !0,
        title: "IDE plugin recommended",
        getTitle: !0,
        displayMessage: "Simplifies Hotswap setup & config"
      };
    if (e === "error")
      return {
        bgClass: E,
        colorClass: "text-ruby-11",
        icon: t.error,
        title: "Hotswap partially enabled",
        displayMessage: "View details"
      };
  }
  resolveIcon(e) {
    return e ? e.getIcon ? this.getIdeIcon() : e.icon : t.bolt;
  }
  resolveTitle(e) {
    return e ? e.combinedTitle ? this.getCombinedTitle() : e.getTitle ? this.getIdePluginName() : e.title : "Development Workflow";
  }
  getUserName() {
    return [i.userInfo?.firstName, i.userInfo?.lastName].filter(Boolean).join(" ") || "Log in";
  }
  getLicenseType() {
    return i.userInfo?.validLicense ? "" : "Unlock all Copilot features, including AI";
  }
  getIdeIcon() {
    switch (i.idePluginState?.ide) {
      case "intellij":
        return t.intelliJ;
      case "vscode":
        return t.vsCode;
      case "eclipse":
        return t.eclipse;
      default:
        return t.code;
    }
  }
  getIdePluginName() {
    switch (i.idePluginState?.ide) {
      case "intellij":
        return "Vaadin plugin for IntelliJ";
      case "vscode":
        return "Vaadin extension for VS Code";
      case "eclipse":
        return "Vaadin plugin for Eclipse";
      default:
        return "IDE plugin";
    }
  }
  getCombinedTitle() {
    switch (i.idePluginState?.ide) {
      case "intellij":
        return "IntelliJ plugin & Hotswap recommended";
      case "vscode":
        return "VS Code extension & Hotswap recommended";
      case "eclipse":
        return "Eclipse plugin & Hotswap recommended";
      default:
        return "IDE plugin & Hotswap recommended";
    }
  }
  closePopover() {
    const e = this.closest("vaadin-popover");
    e && (e.opened = !1);
  }
  handleUserLoginClick() {
    if (i.userInfo?.validLicense) {
      window.open("https://vaadin.com/myaccount", "_blank", "noopener");
      return;
    }
    i.setLoginCheckActive(!0);
  }
  handleDevelopmentWorkflowClick() {
    C("use-dev-workflow-guide"), l.openPanel(I), this.closePopover();
  }
  handleAppInfoClick() {
    l.openPanel(d.INFO), this.closePopover();
  }
  handleAppLogClick() {
    l.openPanel(d.LOG), this.closePopover();
  }
  handleFeaturesClick() {
    l.openPanel(d.FEATURES), this.closePopover();
  }
  handleImpersonateAppUserClick() {
    l.openPanel(d.IMPERSONATOR), this.closePopover();
  }
  handleSettingsClick() {
    l.openPanel(d.SETTINGS), this.closePopover();
  }
  handleHelpAndSupportClick() {
    l.openPanel(d.FEEDBACK), this.closePopover();
  }
};
g = w([
  m("copilot-devtools")
], g);
export {
  g as CopilotDevTools
};
