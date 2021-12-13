import {createWebHistory, createRouter} from "vue-router";
import Home from "@/views/Home.vue";
import Classes from "@/views/Classes";
import Branches from "@/views/Branches";
import Merge from "@/views/Merge";
import Relationships from "@/views/Relationships";
import Individuals from "@/views/Individuals";

const routes = [
    {
        path: "/",
        name: "Home",
        component: Home,
    },
    {
        path: "/classes",
        name: "Classes",
        component: Classes,
        meta: {
            breadcrumbs: [
                {
                    name: "Classes",
                    href: "/classes"
                }
            ]
        },
    },
    {
        path: "/branches",
        name: "Branches",
        component: Branches,
        meta: {
            breadcrumbs: [
                {
                    name: "Branches",
                    href: "/branches"
                }
            ]
        },
    },
    {
        path: "/branches/merge/:branch",
        name: "Merge",
        component: Merge,
    },
    {
        path: "/relationships",
        name: "Relationships",
        component: Relationships,
        meta: {
            breadcrumbs: [
                {
                    name: "Relationships",
                    href: "/relationships"
                }
            ]
        },
    },
    {
        path: "/individuals",
        name: "Individuals",
        component: Individuals,
        meta: {
            breadcrumbs: [
                {
                    name: "Individuals",
                    href: "/individuals"
                }
            ]
        },
    },
];

const router = createRouter({
    routes,
    history: createWebHistory(),
});

export default router;
