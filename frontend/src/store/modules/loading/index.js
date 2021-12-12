const state = {
    isLoading: false,
    loadingText: "",
}
const mutations = {
    UPDATE_IS_LOADING(state, payload) {
        if (typeof payload === 'boolean') {
            state.isLoading = payload;
        } else if(typeof payload === 'string') {
            state.isLoading = true;
            state.loadingText = payload;
        } else {
            if(state.loadingText !== payload.loadingText && !payload.isLoading) return;
            state.isLoading = payload.isLoading;
            state.loadingText = payload.loadingText;
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