import { M as z, p as M, K, aq as j, c as J, ar as A } from "./copilot-Bm5k-h74.js";
class V extends z {
  constructor() {
    super(...arguments), this.eventBusRemovers = [], this.messageHandlers = {}, this.handleESC = (t) => {
      const e = M.getPanelByTag(this.tagName);
      K().appInteractable && e && !e.individual || t.key === "Escape" && j(this);
    };
  }
  /**
   * Preferred panel width in pixels.
   *
   * Subclasses can override this to provide a panel-specific minimum target width
   * for auto-layout updates. Final width is still clamped by viewport and max-width limits.
   */
  getPreferredWidth() {
    return 400;
  }
  /**
   * Preferred panel height in pixels.
   *
   * Subclasses can override this to provide a panel-specific minimum target height
   * for auto-layout updates. Final height is still clamped by viewport and max-height limits.
   */
  getPreferredHeight() {
    return 400;
  }
  /**
   * Preferred panel max width in pixels.
   *
   */
  getPreferredMaxWidth() {
    return 500;
  }
  createRenderRoot() {
    return this;
  }
  onEventBus(t, e) {
    this.eventBusRemovers.push(J.on(t, e));
  }
  connectedCallback() {
    super.connectedCallback(), this.addESCListener();
  }
  disconnectedCallback() {
    super.disconnectedCallback(), this.eventBusRemovers.forEach((t) => t()), this.removeESCListener();
  }
  addESCListener() {
    document.addEventListener("keydown", this.handleESC);
  }
  removeESCListener() {
    document.removeEventListener("keydown", this.handleESC);
  }
  onCommand(t, e) {
    this.messageHandlers[t] = e;
  }
  handleMessage(t) {
    return this.messageHandlers[t.command] ? (this.messageHandlers[t.command].call(this, t), !0) : !1;
  }
  /**
   * Recalculates the position of a vaadin-popover that wraps this panel after content size changes.
   *
   * When content grows (e.g. after properties are loaded), the popover may have been initially
   * placed on a side that no longer offers enough room. This method checks whether the opposite
   * side of the anchor button has more available space and, if so, switches the popover there
   * to minimise scrolling. The popover is left unchanged when:
   * - The current content already fits on the current side, or
   * - The opposite side would offer equal or less space.
   */
  repositionInPopover(t) {
    const e = Math.max(this.scrollHeight, this.offsetHeight);
    if (e === 0)
      return;
    const r = t.getAttribute("for");
    if (!r)
      return;
    const o = t.parentElement?.querySelector(`#${r}`);
    if (!o)
      return;
    const n = o.getBoundingClientRect(), h = 16, d = n.top - h, l = window.innerHeight - h - n.bottom, m = (t.position ?? t.getAttribute("position") ?? "bottom").startsWith("top"), p = m ? d : l, i = m ? l : d;
    p >= e || i <= p || (t.position = m ? "bottom" : "top", t._overlayElement?._updatePosition?.());
  }
  /**
   * Recalculates the panel dialog geometry and persists it to section panel UI state.
   *
   * The method:
   * - Measures rendered content size
   * - Adds dialog chrome offsets (header/content/footer frame)
   * - Applies preferred size hooks (`getPreferredWidth` / `getPreferredHeight`)
   * - Clamps the result to viewport and configured max limits
   * - Repositions relative to the matching toolbar button when available
   *
   * Returns a promise that always resolves (including early-exit paths), so callers
   * can safely await completion before running dependent operations.
   */
  requestLayoutUpdate() {
    const t = this.localName;
    if (M.positionUpdatedManually(t))
      return Promise.resolve();
    const e = M.getPanelByTag(t);
    return e ? new Promise((r) => {
      requestAnimationFrame(() => {
        const o = A(this, "vaadin-dialog");
        if (!o) {
          const x = A(this, "vaadin-popover");
          x && this.repositionInPopover(x), r();
          return;
        }
        const n = this.parentElement?.getBoundingClientRect();
        if (!n || n.width === 0 && n.height === 0) {
          r();
          return;
        }
        const h = o._overlayElement, d = h?.shadowRoot?.querySelector('[part="overlay"]'), l = h?.shadowRoot?.querySelector('[part="content"]'), b = h?.shadowRoot?.querySelector('[part="footer"]');
        if (!d || !l) {
          r();
          return;
        }
        const m = Math.max(this.scrollWidth, this.offsetWidth, n.width), p = Math.max(this.scrollHeight, this.offsetHeight, n.height), i = d.getBoundingClientRect(), v = l.getBoundingClientRect(), W = Math.max(0, i.width - v.width), N = Math.max(0, i.height - v.height), s = b?.getBoundingClientRect(), P = !!s && s.width > 0 && s.height > 0, L = P ? Math.max(0, i.left - s.left) + Math.max(0, s.right - i.right) : 0, q = P ? Math.max(0, i.top - s.top) + Math.max(0, s.bottom - i.bottom) : 0, a = 16, k = this.getPreferredMaxWidth(), G = Math.floor(window.innerHeight * 2 / 3), F = Math.max(0, this.getPreferredWidth()), I = Math.max(0, this.getPreferredHeight()), T = Math.min(k, Math.max(120, window.innerWidth - a * 2)), $ = Math.min(G, Math.max(120, window.innerHeight - a * 2)), f = Math.max(
          120,
          Math.min(
            T,
            Math.max(F, Math.ceil(m + W + L))
          )
        ), u = Math.max(
          120,
          Math.min(
            $,
            Math.max(I, Math.ceil(p + N + q))
          )
        ), C = `${f}px`, E = `${u}px`;
        o.setAttribute("width", C), o.setAttribute("height", E), o.width = C, o.height = E;
        const y = e.position, H = Number.parseFloat(o.getAttribute("top") ?? ""), R = Number.parseFloat(o.getAttribute("left") ?? ""), U = y?.top ?? (Number.isNaN(H) ? 0 : H), _ = y?.left ?? (Number.isNaN(R) ? 0 : R), B = document.querySelector("copilot-main")?.shadowRoot?.querySelector(`copilot-toolbar #${t}-toolbar-btn`);
        let g = U, w = _;
        if (B) {
          const c = B.getBoundingClientRect(), S = c.top - 16 - a, D = window.innerHeight - a - c.bottom - 16;
          S >= u || S >= D ? g = c.top - u - 16 : g = c.bottom + 16, w = c.left + c.width / 2 - f / 2;
        }
        g = Math.max(a, Math.min(g, window.innerHeight - a - u)), w = Math.max(a, Math.min(w, window.innerWidth - a - f)), M.updatePanel(
          t,
          {
            position: {
              top: g,
              left: w,
              width: f,
              height: u
            }
          },
          !1
        ), r();
      });
    }) : Promise.resolve();
  }
}
export {
  V as B
};
