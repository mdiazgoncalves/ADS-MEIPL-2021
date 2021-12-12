<template>
  <div id="app">
    <header>
      <NavBar :breadcrumbs="breadcrumbs"/>
    </header>
    <main>
      <router-view v-slot="{ Component } ">
        <transition name="slide-fade" mode="out-in">
          <keep-alive>
            <component :is="Component"/>
          </keep-alive>
        </transition>
      </router-view>
    </main>
    <div class="lds-facebook" v-if="isLoading">
      <div></div>
      <div></div>
      <div></div>
      <div class="loading-text">{{ loadingText }}</div>
    </div>
  </div>
</template>

<script>
import {computed} from "vue";
import {useStore} from "vuex";
import NavBar from "@/components/NavBar";
import {useRoute, useRouter} from "vue-router";

export default {
  name: 'App',
  components: {NavBar},
  setup() {
    const store = useStore()
    const route = useRoute()
    const router = useRouter()

    router.beforeEach((to, from, next) => {
      if (to.name === "Merge") {
        to.meta.breadcrumbs = [
          {
            name: "Branches",
            href: "/branches"
          },
          {
            name: to.params.branch,
            href: `/branches/merge/${to.params.branch}`
          },
        ]
      }
      next()
    })

    return {
      isLoading: computed(() => store.getters.isLoading),
      loadingText: computed(() => store.getters.loadingText),
      breadcrumbs: computed(() => route.meta.breadcrumbs),
    }
  }
}
</script>

<style>

@import url('https://fonts.googleapis.com/css2?family=Nunito:wght@700&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Roboto:wght@400;500&display=swap');

:root {
  --primary: #6866fa;
  --primary-rgb: 104, 102, 250;
}

button, input[type=button], input[type=submit] {
  height: 40px;
  width: 160px;
  text-transform: uppercase;
}

button:hover, input[type=button]:hover, input[type=submit]:hover {
  cursor: pointer;
}

a:hover {
  cursor: pointer;
  color: var(--primary);
}

button.primary-outline, input[type=button].primary-outline, input[type=submit].primary-outline {
  background-color: transparent;
  color: var(--primary);
  font-size: 14px;
  border: 1px solid var(--primary);
  border-radius: 4px;
}

button.primary-outline:hover, input[type=button].primary-outline:hover, input[type=submit].primary-outline:hover {
  background-color: var(--primary);
  color: white;
  border: none;
}

button.primary, input[type=button].primary, input[type=submit].primary {
  background-color: var(--primary);
  color: white;
  font-size: 14px;
  border: none;
  border-radius: 4px;
}

button.primary:hover, input[type=button].primary:hover, input[type=submit].primary:hover {
  background-color: white;
  color: var(--primary);
  border: 1px solid var(--primary);
}

#app {
  font-family: Roboto, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  font-weight: 400;
  line-height: 1.4;
}

* {
  margin: 0;
  padding: 0;
}

textarea {
  line-height: 1.4;
  font-size: 13px;
  resize: none;
}

a {
  color: black;
  text-decoration: none;
}

body {
  background-color: #F2F6F8;
}

h2 {
  font-family: Nunito, sans-serif;
  font-weight: 700;
  font-size: 16px;
}

.lds-facebook {
  top: calc(50% - 40px);
  left: calc(50% - 40px);
  position: absolute;
  width: 80px;
  height: 80px;
}

.loading-text {
  width: 14em !important;
  left: calc(-7em + 40px) !important;
  background: none !important;
  top: 74px;
  text-align: center;
  animation: none !important;
  font-family: Nunito, sans-serif;
  font-size: 20px;
  color: #494949;
}

.lds-facebook div {
  display: inline-block;
  position: absolute;
  left: 8px;
  width: 16px;
  background: #494949;
  animation: lds-facebook 1.2s cubic-bezier(0, 0.5, 0.5, 1) infinite;
}

.lds-facebook div:nth-child(1) {
  left: 8px;
  animation-delay: -0.24s;
}

.lds-facebook div:nth-child(2) {
  left: 32px;
  animation-delay: -0.12s;
}

.lds-facebook div:nth-child(3) {
  left: 56px;
  animation-delay: 0s;
}

@keyframes lds-facebook {
  0% {
    top: 8px;
    height: 64px;
  }
  50%, 100% {
    top: 24px;
    height: 32px;
  }
}

input {
  height: 40px;
  border: 1px solid #9b9b9b;
  border-radius: 4px;
  box-sizing: border-box;
  padding: 0 16px;
  font-size: 15px;
}

:focus-visible {
  outline: var(--primary) auto 1px !important;
  outline-color: -webkit-focus-ring-color;
  outline-style: auto;
}

.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.2s ease-in;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateX(-10px);
  opacity: 0;
}

header {
  display: flex;
  justify-content: center;
}

main {
  margin-top: 32px;
  margin-bottom: 32px;
}
</style>
