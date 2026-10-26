export const CART_CHANGED = "booqin:cart-changed";

/** Tell the app bar (and anything else listening) that the cart content changed. */
export const notifyCartChanged = () => window.dispatchEvent(new Event(CART_CHANGED));

/**
 * Borrow animation: a copy of the cover travels in an arc to the cart icon and
 * shrinks into it. Transform and opacity only; skipped with reduced motion.
 */
export const flyToCart = (from: HTMLElement | null): void => {
    const target = document.querySelector<HTMLElement>("[data-cart-target]");
    if (!from || !target || window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;

    const a = from.getBoundingClientRect();
    const b = target.getBoundingClientRect();
    const endScale = 24 / a.width;
    const endX = b.left + b.width / 2 - a.left - (a.width * endScale) / 2;
    const endY = b.top + b.height / 2 - a.top - (a.height * endScale) / 2;

    const ghost = from.cloneNode(true) as HTMLElement;
    Object.assign(ghost.style, {
        position: "fixed",
        left: `${a.left}px`,
        top: `${a.top}px`,
        width: `${a.width}px`,
        height: `${a.height}px`,
        margin: "0",
        zIndex: "100",
        pointerEvents: "none",
        transformOrigin: "top left",
    });
    document.body.appendChild(ghost);

    const animation = ghost.animate(
        [
            { transform: "translate(0, 0) scale(1)", opacity: 1 },
            { transform: `translate(${endX * 0.45}px, ${endY * 0.35 - 70}px) scale(0.55)`, opacity: 1, offset: 0.5 },
            { transform: `translate(${endX}px, ${endY}px) scale(${endScale})`, opacity: 0.3 },
        ],
        { duration: 450, easing: "cubic-bezier(0.2, 0, 0, 1)" }
    );
    animation.onfinish = () => ghost.remove();
    // Safety net: never leave the copy behind if the page stops producing frames
    window.setTimeout(() => ghost.remove(), 1000);
};
