const state = {
    token: null,
    branch: null,
    commit: null,
}
const mutations = {
    UPDATE_TOKEN(state, payload) {
        state.token = payload;
    },
    UPDATE_BRANCH(state, payload) {
        state.branch = payload;
    },
    UPDATE_COMMIT(state, payload) {
        state.commit = payload;
    }
}
const actions = {
    setToken({commit}, value) {
        commit('UPDATE_TOKEN', value);
    },
    setBranch({commit}, value) {
        commit('UPDATE_BRANCH', value);
    },
    setCommit({commit}, value) {
        commit('UPDATE_COMMIT', value);
    }
}
const getters = {
    token: state => state.token,
    branch: state => state.branch,
    commit: state => state.commit,
}
const sessionModule = {
    state,
    mutations,
    actions,
    getters
}
export default sessionModule