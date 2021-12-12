<template>
  <div id="branches-container">
    <div :class="{'branch': true, 'main': branch.branchName === 'main'}" v-for="branch in branches"
         :key="branch.branchName"
         @click="selectBranch(branch.branchName)">
      <div class="branch-name-selected">
        <div v-if="selectedBranch === branch.branchName" class="selected"> --></div>
        <h2 class="branch-name">{{ branch.branchName }}</h2>
      </div>
      <div class="info">
        <div class="last-update">Last update: {{ formatDate(branch.lastCommitDate) }}</div>
        <div class="actions" v-if="branch.branchName !== 'main'">
          <div class="merge" @click.stop="merge(branch.branchName)">
            <img :src="mergeIcon" alt="Merge" class="merge-icon"/>
          </div>
          <div class="delete" @click.stop="onDelete(branch.branchName)">&cross;
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {useStore} from "vuex";
import {computed, onActivated, ref, watch} from "vue";
import {useRoute, useRouter} from "vue-router";
import axios from "axios";

export default {
  name: "Branches",
  setup() {
    const router = useRouter()
    const route = useRoute()
    const store = useStore()
    const branches = ref([])
    const selectedBranch = computed(() => store.getters.branch ?? "main")

    onActivated(() => {
      if (store.getters.token == null) {
        router.push("/");
      }
    })

    const fetchBranches = async () => {
      if (store.getters.token == null) return;
      branches.value = []
      await store.dispatch('setLoading', "Loading branches…");
      const response = await axios.get(`https://knowledge-base-ads-test2.herokuapp.com/branches?token=${store.getters.token}`);
      console.log(response);
      branches.value = response.data
      await store.dispatch('setLoading', {loadingText: "Loading branches…", isLoading: false});
    }

    watch(() => store.getters.token, async () => {
      if (route.name === "Branches") {
        await fetchBranches();
      }
    })

    onActivated(async () => await fetchBranches())

    const onDelete = async (branch) => {
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

    const merge = async (branch) => {
      if (branch !== "main") {
        await router.push({name: 'Merge', params: {branch: branch}})
      }
    }

    const selectBranch = async (branch) => {
      await store.dispatch('setBranch', branch);
    }

    const mergeIcon = require("@/assets/img/icons/merge.png")

    const formatDate = (date) => new Date(date).toLocaleString()

    return {
      branches,
      onDelete,
      merge,
      mergeIcon,
      selectedBranch,
      selectBranch,
      formatDate,
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
  align-items: center;
  background-color: white;
  /*box-shadow: rgba(0, 0, 0, 0.04) 0 3px 5px;*/
  border-radius: 32px;
  border: 1px solid #d3d3d3;
  padding: 16px 16px 16px 24px;
  transition: box-shadow 0.2s, border-color 0.2s, color 0.2s;
}

.branch:hover {
  box-shadow: rgba(0, 0, 0, 0.08) 0 3px 5px;
  border-color: var(--primary);
  color: var(--primary);
}

.delete {
  color: #c93b3b;
  background-color: #f1f1f1;
  border-radius: 16px;
  width: 24px;
  height: 24px;
  font-size: 20px;
  padding: 4px;
  text-align: center;
  line-height: 24px;
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
  gap: 8px;
  align-items: center;
  margin-left: 4px;
}

.info {
  display: flex;
  align-items: center;
}

.selected {
  font-size: 16px;
  text-transform: uppercase;
  color: var(--primary);
  font-weight: 500;
}

.merge-icon {
  cursor: pointer;
  width: 20px;
  height: 20px;
  padding: 6px;
  background-color: #f1f1f1;
  border-radius: 16px;
  vertical-align: middle;
}

.merge-icon:hover {
  background-color: #cde2ff;
}

.last-update {
  color: black !important;
  font-size: 15px;
  margin-right: 8px;
}

.branch-name-selected {
  display: flex;
  gap: 8px;
}
</style>