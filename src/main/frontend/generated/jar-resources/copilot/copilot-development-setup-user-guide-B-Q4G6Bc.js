import { i as s } from "./icons-mWh87_oB.js";
import { am as j, K as k, p as b, N as r, q as c, Q as f, c as I, x as n, a9 as S, an as P, E as p, R as A, e as J, h as H, P as y, ao as D, ap as E, n as M } from "./copilot-Bm5k-h74.js";
import { r as g } from "./state-BH_9RcTJ.js";
import { B as R } from "./base-panel-Cxtbncvf.js";
var V = Object.defineProperty, O = Object.getOwnPropertyDescriptor, h = (e, t, a, i) => {
  for (var o = i > 1 ? void 0 : i ? O(t, a) : t, l = e.length - 1, d; l >= 0; l--)
    (d = e[l]) && (o = (i ? d(t, a, o) : d(o)) || o);
  return i && o && V(t, a, o), o;
};
const C = "https://github.com/JetBrains/JetBrainsRuntime/releases";
function U(e, t) {
  if (!t)
    return !0;
  const [a, i, o] = t.split(".").map((m) => parseInt(m)), [l, d, v] = e.split(".").map((m) => parseInt(m));
  if (a < l)
    return !0;
  if (a === l) {
    if (i < d)
      return !0;
    if (i === d)
      return o < v;
  }
  return !1;
}
const $ = "Download complete";
let u = class extends R {
  constructor() {
    super(), this.javaPluginSectionOpened = !1, this.hotswapSectionOpened = !1, this.hotswapTab = "hotswapagent", this.downloadStatusMessages = [], this.downloadProgress = 0, this.onDownloadStatusUpdate = this.downloadStatusUpdate.bind(this), this.handleESC = (e) => {
      k().appInteractable || e.key === "Escape" && b.openPanel(w.tag);
    }, this.reaction(
      () => [r.jdkInfo, c.idePluginState],
      () => {
        c.idePluginState && (!c.idePluginState.ide || !c.idePluginState.active ? this.javaPluginSectionOpened = !0 : (!(/* @__PURE__ */ new Set(["vscode", "intellij"])).has(c.idePluginState.ide) || !c.idePluginState.active) && (this.javaPluginSectionOpened = !1)), r.jdkInfo && f() !== "success" && (this.hotswapSectionOpened = !0);
      },
      { fireImmediately: !0 }
    );
  }
  createRenderRoot() {
    return this;
  }
  connectedCallback() {
    super.connectedCallback(), I.on("set-up-vs-code-hotswap-status", this.onDownloadStatusUpdate);
  }
  disconnectedCallback() {
    super.disconnectedCallback(), I.off("set-up-vs-code-hotswap-status", this.onDownloadStatusUpdate);
  }
  render() {
    const e = {
      intellij: c.idePluginState?.ide === "intellij",
      vscode: c.idePluginState?.ide === "vscode",
      eclipse: c.idePluginState?.ide === "eclipse",
      idePluginInstalled: !!c.idePluginState?.active
    };
    return n`
      <div part="container">${this.renderPluginSection(e)} ${this.renderHotswapSection(e)}</div>
      <div part="footer">
        <vaadin-button
          id="close"
          @click="${() => b.closePanel(w.tag)}"
          >Close
        </vaadin-button>
      </div>
    `;
  }
  renderPluginSection(e) {
    let t = "";
    e.intellij ? t = "IntelliJ" : e.vscode ? t = "VS Code" : e.eclipse && (t = "Eclipse");
    let a, i;
    e.vscode || e.intellij ? e.idePluginInstalled ? (a = `Plugin for ${t} installed`, i = this.renderPluginInstalledContent()) : (a = `Plugin for ${t} not installed`, i = this.renderPluginIsNotInstalledContent(e)) : e.eclipse ? (a = e.idePluginInstalled ? "Eclipse plugin installed" : "Eclipse plugin not installed", i = e.idePluginInstalled ? this.renderPluginInstalledContent() : this.renderEclipsePluginContent()) : (a = "No IDE found", i = this.renderNoIdePluginContent());
    const o = e.idePluginInstalled ? s.checkCircle : s.alertTriangle;
    return n`
      <details
        part="panel"
        .open=${this.javaPluginSectionOpened}
        @toggle=${(l) => {
      S(() => {
        this.javaPluginSectionOpened = l.target.open;
      }), this.requestLayoutUpdate();
    }}>
        <summary part="header">
          <span class="icon ${e.idePluginInstalled ? "success" : "warning"}">${o}</span>
          <div>${a}</div>
        </summary>
        <div part="content">${i}</div>
      </details>
    `;
  }
  renderNoIdePluginContent() {
    return n`
      <div>
        <div>We could not detect an IDE</div>
        ${this.recommendSupportedPlugin()}
      </div>
    `;
  }
  renderEclipsePluginContent() {
    return n`
      <div>
        <div>Install the Vaadin Eclipse Plugin to ensure a smooth development workflow</div>
        <p>
          Installing the plugin is not required, but strongly recommended.<br />Some Vaadin Copilot functionality, such
          as undo, will not function optimally without the plugin.
        </p>
        <div>
          <vaadin-button
            @click="${() => {
      window.open(P, "_blank");
    }}"
            >Install from Eclipse Marketplace
            <vaadin-icon slot="suffix" icon="vaadin:external-link"></vaadin-icon>
          </vaadin-button>
        </div>
      </div>
    `;
  }
  recommendSupportedPlugin() {
    return n`<div>
      Please use <a href="https://code.visualstudio.com">Visual Studio Code</a> or
      <a href="https://www.jetbrains.com/idea">IntelliJ IDEA</a> for better development experience
    </div>`;
  }
  renderPluginInstalledContent() {
    return n` <div>You have a running plugin. Enjoy your awesome development workflow!</div> `;
  }
  renderPluginIsNotInstalledContent(e) {
    let t = null, a = "Install from Marketplace";
    return e.intellij ? (t = D, a = "Install from JetBrains Marketplace") : e.vscode ? (t = E, a = "Install from VSCode Marketplace") : e.eclipse && (t = P, a = "Install from Eclipse Marketplace"), n`
      <div>
        <div>Install the Vaadin IDE Plugin to ensure a smooth development workflow</div>
        <p>
          Installing the plugin is not required, but strongly recommended.<br />Some Vaadin Copilot functionality, such
          as undo, will not function optimally without the plugin.
        </p>
        ${t ? n` <div>
              <vaadin-button
                @click="${() => {
      window.open(t, "_blank");
    }}"
                >${a}
                <vaadin-icon slot="suffix" icon="vaadin:external-link"></vaadin-icon>
              </vaadin-button>
            </div>` : p}
      </div>
    `;
  }
  renderHotswapSection(e) {
    const { jdkInfo: t } = r;
    if (!t)
      return p;
    const a = f(), i = A();
    let o, l, d;
    return a === "success" ? (o = s.checkCircle, d = "Java Hotswap is enabled") : a === "warning" ? (o = s.alertTriangle, d = "Java Hotswap is not enabled") : a === "error" && (o = s.alertTriangle, d = "Java Hotswap is partially enabled"), this.hotswapTab === "jrebel" ? t.jrebel ? l = this.renderJRebelInstalledContent() : l = this.renderJRebelNotInstalledContent() : e.intellij ? l = this.renderHotswapAgentPluginContent(this.renderIntelliJHotswapHint) : e.vscode ? l = this.renderHotswapAgentPluginContent(this.renderVSCodeHotswapHint) : l = this.renderHotswapAgentNotInstalledContent(e), n` <details
      part="panel"
      .open=${this.hotswapSectionOpened}
      @toggle=${(v) => {
      S(() => {
        this.hotswapSectionOpened = v.target.open;
      }), this.requestLayoutUpdate();
    }}>
      <summary part="header">
        <span class="icon ${a}">${o}</span>
        <div>${d}</div>
      </summary>
      <div part="content">
        ${i !== "none" ? n`${i === "jrebel" ? this.renderJRebelInstalledContent() : this.renderHotswapAgentInstalledContent()}` : n`
            <div class="tabs" role="tablist">
              <button
                aria-selected="${this.hotswapTab === "hotswapagent" ? "true" : "false"}"
                class="tab"
                role="tab"
                @click=${() => {
      this.hotswapTab = "hotswapagent";
    }}>
                Hotswap Agent
              </button>
              <button
                aria-selected="${this.hotswapTab === "jrebel" ? "true" : "false"}"
                class="tab"
                role="tab"
                @click=${() => {
      this.hotswapTab = "jrebel";
    }}>
                JRebel
              </button>
            </div>
            <div part="content">${l}</div>
            </div>
            </details>
          `}
      </div>
    </details>`;
  }
  renderJRebelNotInstalledContent() {
    return n`
      <div>
        <a href="https://www.jrebel.com">JRebel ${s.arrowOutward}</a> is a commercial hotswap solution. Vaadin
        detects the JRebel Agent and automatically reloads the application in the browser after the Java changes have
        been hotpatched.
        <p>
          Go to
          <a href="https://www.jrebel.com/products/jrebel/learn" target="_blank" rel="noopener noreferrer">
            https://www.jrebel.com/products/jrebel/learn ${s.arrowOutward}</a
          >
          to get started
        </p>
      </div>
    `;
  }
  renderHotswapAgentNotInstalledContent(e) {
    const t = [
      this.renderJavaRunningInDebugModeSection(),
      this.renderHotswapAgentJdkSection(e),
      this.renderInstallHotswapAgentJdkSection(e),
      this.renderHotswapAgentVersionSection(),
      this.renderHotswapAgentMissingArgParam(e)
    ];
    return n` <div part="hotswap-agent-section-container">${t}</div> `;
  }
  renderHotswapAgentPluginContent(e) {
    const a = f() === "success";
    return n`
      <div part="hotswap-agent-section-container">
        <div class="inner-section">
          <span class="hotswap icon ${a ? "success" : "warning"}"
            >${a ? s.checkCircle : s.alertTriangle}</span
          >
          ${e()}
        </div>
      </div>
    `;
  }
  renderIntelliJHotswapHint() {
    return n` <div class="hint">
      <h3>Use 'Debug using Hotswap Agent' launch configuration</h3>
      Vaadin IntelliJ plugin offers launch mode that does not require any manual configuration!
      <p>
        In order to run recommended launch configuration, you should click three dots right next to Debug button and
        select <code>Debug using Hotswap Agent</code> option.
      </p>
    </div>`;
  }
  renderVSCodeHotswapHint() {
    return n` <div class="hint">
      <h3>Use 'Debug (hotswap)'</h3>
      With Vaadin Visual Studio Code extension you can run Hotswap Agent without any manual configuration required!
      <p>Click <code>Debug (hotswap)</code> within your main class to debug application using Hotswap Agent.</p>
    </div>`;
  }
  renderJavaRunningInDebugModeSection() {
    const e = r.jdkInfo?.runningInJavaDebugMode;
    return n`
      <div class="inner-section">
        <details class="inner" .open="${!e}">
          <summary>
            <span class="icon ${e ? "success" : "warning"}"
              >${e ? s.checkCircle : s.alertTriangle}</span
            >
            <div>Run Java in debug mode</div>
          </summary>
          <div class="hint">Start the application in debug mode in the IDE</div>
        </details>
      </div>
    `;
  }
  renderHotswapAgentMissingArgParam(e) {
    const t = r.jdkInfo?.runningWitHotswap && r.jdkInfo?.runningWithExtendClassDef;
    return n`
      <div class="inner-section">
        <details class="inner" .open="${!t}">
          <summary>
            <span class="icon ${t ? "success" : "warning"}"
              >${t ? s.checkCircle : s.alertTriangle}</span
            >
            <div>Enable HotswapAgent</div>
          </summary>
          <div class="hint">
            <ul>
              ${e.intellij ? n`<li>Launch as mentioned in the previous step</li>` : p}
              ${e.intellij ? n`<li>
                    To manually configure IntelliJ, add the
                    <code>-XX:HotswapAgent=fatjar -XX:+AllowEnhancedClassRedefinition -XX:+UpdateClasses</code> JVM
                    arguments when launching the application
                  </li>` : n`<li>
                    Add the
                    <code>-XX:HotswapAgent=fatjar -XX:+AllowEnhancedClassRedefinition -XX:+UpdateClasses</code> JVM
                    arguments when launching the application
                  </li>`}
            </ul>
          </div>
        </details>
      </div>
    `;
  }
  renderHotswapAgentJdkSection(e) {
    const t = r.jdkInfo?.extendedClassDefCapable, a = this.downloadStatusMessages?.[this.downloadStatusMessages.length - 1] === $;
    return n`
      <div class="inner-section">
        <details class="inner" .open="${!t}">
          <summary>
            <span class="icon ${t ? "success" : "warning"}"
              >${t ? s.checkCircle : s.alertTriangle}</span
            >
            <div>Run using JetBrains Runtime JDK</div>
          </summary>
          <div class="hint">
            JetBrains Runtime provides much better hotswapping compared to other JDKs.
            <ul>
              ${e.intellij && U("1.3.0", c.idePluginState?.version) ? n` <li>Upgrade to the latest IntelliJ plugin</li>` : p}
              ${e.intellij ? n` <li>Launch the application in IntelliJ using "Debug using Hotswap Agent"</li>` : p}
              ${e.vscode ? n` <li>
                    <a href @click="${(i) => this.downloadJetbrainsRuntime(i)}"
                      >Let Copilot download and set up JetBrains Runtime for VS Code</a
                    >
                    ${this.downloadProgress > 0 ? n`<vaadin-progress-bar
                          .value="${this.downloadProgress}"
                          min="0"
                          max="1"></vaadin-progress-bar>` : p}
                    <ul>
                      ${this.downloadStatusMessages.map((i) => n`<li>${i}</li>`)}
                      ${a ? n`<h3>Go to VS Code and launch the 'Debug using Hotswap Agent' configuration</h3>` : p}
                    </ul>
                  </li>` : p}
              <li>
                ${e.intellij || e.vscode ? n`If there is a problem, you can manually
                      <a target="_blank" href="${C}">download JetBrains Runtime JDK</a> and set up
                      your debug configuration to use it.` : n`<a target="_blank" href="${C}">Download JetBrains Runtime JDK</a> and set up
                      your debug configuration to use it.`}
              </li>
            </ul>
          </div>
        </details>
      </div>
    `;
  }
  renderInstallHotswapAgentJdkSection(e) {
    const t = r.jdkInfo?.hotswapAgentFound, a = r.jdkInfo?.extendedClassDefCapable;
    return n`
      <div class="inner-section">
        <details class="inner" .open="${!t}">
          <summary>
            <span class="icon ${t ? "success" : "warning"}"
              >${t ? s.checkCircle : s.alertTriangle}</span
            >
            <div>Install HotswapAgent</div>
          </summary>
          <div class="hint">
            Hotswap Agent provides application level support for hot reloading, such as reinitalizing Vaadin @Route or
            @BrowserCallable classes when they are updated
            <ul>
              ${e.intellij ? n`<li>Launch as mentioned in the previous step</li>` : p}
              ${!e.intellij && !a ? n`<li>First install JetBrains Runtime as mentioned in the step above.</li>` : p}
              ${e.intellij ? n`<li>
                    To manually configure IntelliJ, download HotswapAgent and install the jar file as
                    <code>[JAVA_HOME]/lib/hotswap/hotswap-agent.jar</code> in the JetBrains Runtime JDK. Note that the
                    file must be renamed to exactly match this path.
                  </li>` : n`<li>
                    Download HotswapAgent and install the jar file as
                    <code>[JAVA_HOME]/lib/hotswap/hotswap-agent.jar</code> in the JetBrains Runtime JDK. Note that the
                    file must be renamed to exactly match this path.
                  </li>`}
            </ul>
          </div>
        </details>
      </div>
    `;
  }
  renderHotswapAgentVersionSection() {
    if (!r.jdkInfo?.hotswapAgentFound)
      return p;
    const e = r.jdkInfo?.hotswapVersionOk, t = r.jdkInfo?.hotswapVersion, a = r.jdkInfo?.hotswapAgentLocation;
    return n`
      <div class="inner-section">
        <details class="inner" .open="${!e}">
          <summary>
            <span class="icon ${e ? "success" : "warning"}"
              >${e ? s.checkCircle : s.alertTriangle}</span
            >
            <div>Hotswap version requires update</div>
          </summary>
          <div class="hint">
            HotswapAgent version ${t} is in use
            <a target="_blank" href="https://github.com/HotswapProjects/HotswapAgent/releases"
              >Download the latest HotswapAgent</a
            >
            and place it in <code>${a}</code>
          </div>
        </details>
      </div>
    `;
  }
  renderJRebelInstalledContent() {
    return n` <div>JRebel is in use. Enjoy your awesome development workflow!</div> `;
  }
  renderHotswapAgentInstalledContent() {
    return n` <div>Hotswap agent is in use. Enjoy your awesome development workflow!</div> `;
  }
  async downloadJetbrainsRuntime(e) {
    return e.target.disabled = !0, e.preventDefault(), this.downloadStatusMessages = [], J(`${y}set-up-vs-code-hotswap`, {}, (t) => {
      t.data.error ? (H("Error downloading JetBrains runtime", t.data.error), this.downloadStatusMessages = [...this.downloadStatusMessages, "Download failed"]) : this.downloadStatusMessages = [...this.downloadStatusMessages, $];
    });
  }
  downloadStatusUpdate(e) {
    const t = e.detail.progress;
    t ? this.downloadProgress = t : this.downloadStatusMessages = [...this.downloadStatusMessages, e.detail.message];
  }
};
u.NAME = "copilot-development-setup-user-guide";
h([
  g()
], u.prototype, "javaPluginSectionOpened", 2);
h([
  g()
], u.prototype, "hotswapSectionOpened", 2);
h([
  g()
], u.prototype, "hotswapTab", 2);
h([
  g()
], u.prototype, "downloadStatusMessages", 2);
h([
  g()
], u.prototype, "downloadProgress", 2);
u = h([
  M(u.NAME)
], u);
const w = {
  header: "Development Workflow",
  tag: j,
  individual: !0
}, _ = {
  init(e) {
    e.addPanel(w);
  }
};
window.Vaadin.copilot.plugins.push(_);
b.addPanel(w);
export {
  u as CopilotDevelopmentSetupUserGuide,
  w as copilotDevelopmentSetupPanelConfig
};
