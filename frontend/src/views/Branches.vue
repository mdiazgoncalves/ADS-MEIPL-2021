<template>
  <div id="branches-container">
    <div :class="{'branch': true, 'main': branch === 'main'}" v-for="branch in branches" :key="branch" @click="merge(branch)">
      <h2 class="branch-name">{{ branch }}</h2>
      <div class="actions" v-if="branch !== 'main'">
        <div class="delete" @click="onDelete(branch)">&cross;</div>
      </div>
    </div>
  </div>
</template>

<script>
import {useStore} from "vuex";
import {onActivated, ref, watch} from "vue";
import {useRouter} from "vue-router";
import axios from "axios";

export default {
  name: "Branches",
  setup() {
    const router = useRouter()
    const store = useStore()
    const branches = ref([])

    onActivated(() => {
      if(store.getters.token == null) {
        router.push("/");
      }
    })

    const fetchBranches = async () => {
      if(store.getters.token == null) return;
      branches.value = []
      await store.dispatch('setLoading', "Loading branches…");
      const response = await axios.get(`https://knowledge-base-ads-test2.herokuapp.com/branches?token=${store.getters.token}`);
      console.log(response);
      branches.value = response.data
      await store.dispatch('setLoading', {loadingText: "Loading branches…", isLoading: false});
    }

    watch(() => store.getters.token, async () => {
      await fetchBranches();
    })

    onActivated(async () => await fetchBranches())

    const onDelete = async(branch) => {
      await store.dispatch('setLoading', `Deleting branch ${branch}…`);
      try {
        const latestResponse = await axios.get(`https://knowledge-base-ads-test2.herokuapp.com/branch/${branch}/latest?token=${store.getters.token}`);
        const commit = latestResponse.data.latestCommit
        console.log(latestResponse)
        const response = await axios.delete(`https://knowledge-base-ads-test2.herokuapp.com/branch/${branch}?commit=${commit}&token=${store.getters.token}`);
        console.log(response)
        await fetchBranches();
      } catch (e) {
        //
      }
      await store.dispatch('setLoading', {loadingText: `Deleting branch ${branch}…`, isLoading: false});
    }

    const merge = async(branch) => {
      if(branch !== "main") {
        await router.push({name: 'Merge', params: {branch: branch}})
      }
    }

    return {
      branches,
      onDelete,
      merge,
    }
  }
}
</script>

<style scoped>
#branches-container {
  display: flex;
  gap: 8px;
  flex-direction: column;
  width: 70%;
  margin: 0 auto;
}

.branch {
  display: flex;
  justify-content: space-between;
  background-color: white;
  /*box-shadow: rgba(0, 0, 0, 0.04) 0 3px 5px;*/
  border-radius: 32px;
  border: 1px solid #d3d3d3;
  padding: 16px 16px 16px 24px;
  transition: box-shadow 0.2s, border-color 0.2s, color 0.2s;
}

.branch:not(.main) {
  cursor: pointer;
}

.branch:not(.main):hover {
  box-shadow: rgba(0, 0, 0, 0.08) 0 3px 5px;
  border-color: var(--primary);
  color: var(--primary);
}

.delete {
  color: #c93b3b;
  background-color: #f1f1f1;
  border-radius: 16px;
  padding: 0 8px;
  margin-left: 8px;
}

.delete:hover {
  cursor: pointer;
  color: #f1f1f1;
  background-color: #c93b3b;
}

.main {
  order: -1;
}

.actions {
  display: flex;
}
</style>