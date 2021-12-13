<template>
  <nav>
    <ul>
      <li class="logo">
        <router-link to="/">Knowledge Base</router-link>
      </li>
      <li class="breadcrumbs" v-if="breadcrumbs.length > 0">
        <ul>
          <li v-for="(breadcrumb, index) in breadcrumbs" :key="index">
            <router-link
                :to="breadcrumb.href">{{ breadcrumb.name }}
            </router-link>
          </li>
        </ul>
      </li>
    </ul>
    <ul>
      <li v-if="branch == null && token == null">
        <button class="primary-outline" @click="isEditVisible = !isEditVisible" id="edit-button">
          Edit
        </button>
      </li>
      <li id="editing-mode" v-if="branch != null || token != null">
        <div id="editing-mode-container">
          <div class="title">{{ token == null ? "Editing mode" : "Curator mode" }}</div>
          <div class="branch" v-if="branch != null">{{ branch }}</div>
        </div>
        <div id="edit-buttons">
          <button class="primary-outline reject" @click="reject()">
            &cross;
          </button>
        </div>
      </li>
    </ul>
    <div id="edit-container" v-if="isEditVisible">
      <div id="edit-help">{{
          'Please enter your e-mail to register your changes to the knowledge base.'
        }}
      </div>
      <form id="edit-form" @submit.prevent="edit">
        <div class="flex-container">
          <input placeholder="E-mail" type="text" v-model="email" name="email"/>
          <input type="submit" value="Edit" class="primary"/>
        </div>
      </form>
      <div id="edit-error" v-if="editError.length > 0">{{ editError }}</div>
    </div>
  </nav>
</template>

<script>
import {computed, inject, onUpdated, ref, toRefs} from "vue";
import validator from 'email-validator';
import {useStore} from "vuex";

export default {
  name: "NavBar",
  props: {
    breadcrumbs: {type: Array, default: () => []},
  },
  setup(props) {
    const {breadcrumbs} = toRefs(props)
    const isEditVisible = ref(false)
    const editError = ref("")
    const email = ref("")
    const store = useStore()
    const axios = inject('axios');

    const positionEditContainer = () => {
      const editButton = document.getElementById("edit-button");
      const editContainer = document.getElementById("edit-container");
      if (!editButton || !editContainer) return;
      const editButtonRect = editButton.getBoundingClientRect();
      const editContainerRect = editContainer.getBoundingClientRect();
      editContainer.style.top = `${editButtonRect.bottom + 2}px`;
      editContainer.style.left = `${editButtonRect.right - editContainerRect.width}px`;
    };
    window.addEventListener("resize", positionEditContainer);
    window.addEventListener("mousedown", e => {
      const editContainer = document.getElementById("edit-container");
      const editButton = document.getElementById("edit-button");
      if (editContainer && editButton && isEditVisible.value === true && !editContainer.contains(e.target) && !editButton.contains(e.target)) {
        isEditVisible.value = false
      }
    });

    onUpdated(positionEditContainer);

    const edit = async () => {
      await store.dispatch("setLoading", {loadingText: "Preparing editing mode…", loadingId: 100, isLoading: true});
      editError.value = "";
      console.log(email.value)
      if (validator.validate(email.value)) {
        try {
          const response = await axios.post(`${process.env.VUE_APP_BACKEND}/branch/${email.value}`);
          await store.dispatch("setBranch", email.value);
          isEditVisible.value = false;
          console.log(response);
        } catch (e) {
          if (e.response.status === 400) {
            await store.dispatch("setBranch", email.value);
            isEditVisible.value = false;
          } else {
            editError.value = "Unknown error, please try again later.";
          }
        }
      } else {
        try {
          const response = await axios.post(`${process.env.VUE_APP_BACKEND}/login/curator?password=${email.value}`);
          console.log(response);
          const token = response.data;
          console.log(token);
          await store.dispatch("setToken", token);
          await store.dispatch("setBranch", "main");
          isEditVisible.value = false;
        } catch (e) {
          console.log(e.response);
          if (e.response.status === 401) {
            editError.value = "Invalid email address.";
          } else {
            editError.value = "Unknown error, please try again later.";
          }
        }
      }
      await store.dispatch("setLoading", {loadingId: 100, isLoading: false});
    }

    const reject = async () => {
      await store.dispatch("setBranch", null);
      await store.dispatch("setToken", null);
    }

    return {
      breadcrumbs,
      isEditVisible,
      email,
      editError,
      edit,
      reject,
      branch: computed(() => store.getters.branch),
      token: computed(() => store.getters.token),
    }
  }
}
</script>

<style scoped>
nav {
  display: flex;
  justify-content: space-between;
  width: 70%;
  margin-top: 32px;
}

nav > ul {
  display: flex;
  align-items: center;
}

nav > ul > li {
  display: block;
  text-transform: uppercase;
  color: #494949;
}

nav > ul > li > a:not(:hover) {
  color: #494949;
}

nav > ul > li:not(:last-child) {
  padding-right: 40px;
}

nav .breadcrumbs > ul {
  display: flex;
}

nav .breadcrumbs > ul > li {
  display: flex;
  padding-right: 8px;
}

nav .breadcrumbs > ul > li::before {
  content: " / ";
  padding-right: 8px;
}

nav > ul > li ul > li > a {
  color: var(--primary);
}

nav .logo, nav .logo a {
  font-family: Nunito, sans-serif;
  font-weight: 700;
  font-size: 20px;
  color: black !important;
}

nav .logo {
  padding-right: 12px !important;
}

#edit-container {
  position: absolute;
  background: white;
  border-radius: 8px;
  box-shadow: rgba(0, 0, 0, 0.2) 0 12px 28px 0, rgba(0, 0, 0, 0.1) 0 2px 4px 0, rgba(255, 255, 255, 0.05) 0 0 0 1px inset;
  width: 340px;
  padding: 8px;
  z-index: 1000;
}

#edit-container .flex-container {
  display: flex;
  justify-content: center;
  margin-bottom: 8px;
}

#edit-container form input[type=text], form input[type=email], form input[type=password] {
  display: inline-block;
  width: 220px;
  margin-right: 8px;
}

#edit-container form input[type=submit] {
  display: inline-block;
  width: 100px;
}

#edit-error {
  display: inline-block;
  color: white;
  background: #d96a6a;
  border-left: solid 4px #831818;
  border-radius: 4px;
  padding: 8px 12px;
  font-size: 14px;
  width: calc(100% - 16px - 24px);
  vertical-align: middle;
  margin: 8px 8px 8px 8px;
}

#edit-help {
  margin: 8px 8px 12px;
  font-size: 14px;
}

#edit-buttons {
  display: flex;
  gap: 8px;
}

#edit-buttons button {
  width: 40px;
  border-radius: 16px;
}

.reject {
  font-size: 20px;
}

#editing-mode {
  display: flex;
  align-items: center;
  gap: 16px;
  border: var(--primary);
}

#editing-mode-container {
  display: flex;
  flex-direction: column;

}

#editing-mode-container .branch {
  font-size: 12px;
}
</style>