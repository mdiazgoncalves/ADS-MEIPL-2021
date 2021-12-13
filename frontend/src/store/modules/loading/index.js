const state = {
    isLoading: false,
    loadingText: "",
    loadingIds: []
}
const mutations = {
    UPDATE_IS_LOADING(state, payload) {
        if (payload.isLoading) {
            state.isLoading = true
            state.loadingIds.push(payload.loadingId)
            state.loadingText = payload.loadingText ?? "";
        } else {
            state.loadingIds = state.loadingIds.filter(it => it !== payload.loadingId)
            if(state.loadingIds.length === 0) {
                state.isLoading = false
            }
        }
    }
}
const actions = {
    setLoading({commit}, value) {
        commit('UPDATE_IS_LOADING', value);
    }
}
const getters = {
    isLoading: state => state.isLoading,
    loadingText: state => state.loadingText,
}
const loadingModule = {
    state,
    mutations,
    actions,
    getters
}
export default loadingModule