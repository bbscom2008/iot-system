
export function parseValueFromEvent(e) {
    if(e.detail && e.detail.__args__ && e.detail.__args__.length > 0) {
        return e.detail.__args__[0];
    }
    return e
}